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
}
