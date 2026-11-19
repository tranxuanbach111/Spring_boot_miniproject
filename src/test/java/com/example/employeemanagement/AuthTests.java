package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.model.User;
import java.time.Instant;
import com.jayway.jsonpath.JsonPath;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.datasource.url=jdbc:h2:mem:auth_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwords;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtEncoder encoder;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        users.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void registersAndLogsInWithSession() throws Exception {
        var csrfResult = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        CsrfToken token = (CsrfToken) csrfResult.getRequest().getAttribute(CsrfToken.class.getName());
        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession();

        mvc.perform(post("/api/auth/register").session(session)
                        .header(token.getHeaderName(), token.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"password123","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        var user = users.findByUsername("alice").orElseThrow();
        assertFalse(user.getPassword().equals("password123"));
        assertTrue(passwords.matches("password123", user.getPassword()));

        var login = mvc.perform(post("/api/auth/login").session(session)
                        .header(token.getHeaderName(), token.getToken())
                        .param("username", "Alice").param("password", "password123"))
                .andExpect(status().isNoContent()).andReturn();
        MockHttpSession loggedIn = (MockHttpSession) login.getRequest().getSession();
        mvc.perform(get("/api/auth/me").session(loggedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
        mvc.perform(post("/api/auth/logout").session(loggedIn).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsDuplicatesInvalidInputAndInvalidLogin() throws Exception {
        String body = """
                {"username":"alice","password":"password123"}
                """;
        mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"a","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").with(csrf())
                        .param("username", "alice").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanReadButCannotWriteWithJwtOrSession() throws Exception {
        users.saveAndFlush(new User("reader", passwords.encode("password123"), User.Role.USER));
        String token = tokenFor("reader");
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/employees/count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/employees").with(httpBasic("reader", "password123")))
                .andExpect(status().isOk());

        String body = """
                {"name":"Denied","email":"denied@example.com","departmentId":1}
                """;
        mvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/employees/999").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/employees/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        var login = mvc.perform(post("/api/auth/login").with(csrf())
                        .param("username", "reader").param("password", "password123"))
                .andExpect(status().isNoContent()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession();
        mvc.perform(get("/employees/list").session(session)).andExpect(status().isOk());
        mvc.perform(get("/employees/add").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/employees/add").session(session).with(csrf())
                        .param("name", "Denied").param("email", "denied@example.com").param("departmentId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUpdateAndDeleteWithJwt() throws Exception {
        users.saveAndFlush(new User("admin", passwords.encode("password123"), User.Role.ADMIN));
        jdbc.update("INSERT INTO department (id, name) VALUES (?, ?)", 900L, "Security Test");
        String token = tokenFor("admin");
        try {
            var result = mvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Admin Test","email":"admin.test@example.com","departmentId":900}
                                    """))
                    .andExpect(status().isCreated()).andReturn();
            Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
            mvc.perform(put("/api/employees/" + id).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Admin Updated","email":"admin.updated@example.com","departmentId":900}
                                    """))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Admin Updated"));
            mvc.perform(delete("/api/employees/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        } finally {
            jdbc.update("DELETE FROM employee WHERE department_id = ?", 900L);
            jdbc.update("DELETE FROM department WHERE id = ?", 900L);
        }
        mvc.perform(get("/employees/add").with(httpBasic("admin", "password123")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidExpiredAndTamperedTokens() throws Exception {
        users.saveAndFlush(new User("reader", passwords.encode("password123"), User.Role.USER));
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"reader","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/employees").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());

        String token = tokenFor("reader");
        String[] parts = token.split("\\.");
        char replacement = parts[2].charAt(0) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "." + replacement + parts[2].substring(1);
        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());

        JwtClaimsSet expired = JwtClaimsSet.builder().issuer("employee-management").subject("reader")
                .issuedAt(Instant.now().minusSeconds(3600)).expiresAt(Instant.now().minusSeconds(120))
                .claim("roles", java.util.List.of("USER")).build();
        String expiredToken = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), expired)).getTokenValue();
        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    private String tokenFor(String username) throws Exception {
        var result = mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

}
