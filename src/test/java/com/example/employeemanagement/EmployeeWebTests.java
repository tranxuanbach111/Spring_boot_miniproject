package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:employee_web_test;DB_CLOSE_DELAY=-1",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class EmployeeWebTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc.update("INSERT INTO department (id, name) VALUES (?, ?)", 100L, "IT");
    }

    @Test
    void rendersListAndAddForm() throws Exception {
        mvc.perform(get("/employees/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("IT")));
    }

    @Test
    void addsEmployeeThenRedirectsToList() throws Exception {
        mvc.perform(post("/employees/add")
                        .param("name", "Web Employee")
                        .param("email", "web@example.com")
                        .param("departmentId", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/list"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM employee WHERE email = ?", Integer.class, "web@example.com"));
    }

    @Test
    void redisplaysInvalidFormWithoutSaving() throws Exception {
        mvc.perform(post("/employees/add")
                        .param("name", " ")
                        .param("email", "invalid")
                        .param("departmentId", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("employee", "name", "email", "departmentId"));
        mvc.perform(post("/employees/add")
                        .param("name", "An")
                        .param("email", "an@example.com")
                        .param("departmentId", "999"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("employee", "departmentId"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class));
    }
}
