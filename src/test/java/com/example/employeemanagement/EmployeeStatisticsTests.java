package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.service.EmployeeReportService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.datasource.url=jdbc:h2:mem:statistics_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class EmployeeStatisticsTests {

    @Autowired
    private DepartmentRepository departments;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmployeeReportService reports;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void countsByDepartmentIncludingEmptyAndDuplicateNames() {
        seedEmployees();
        var rows = departments.countEmployeesByDepartment();
        assertEquals(3, rows.size());
        assertEquals("HR", rows.get(0).getDepartmentName());
        assertEquals(0, rows.get(0).getEmployeeCount());
        assertEquals(1, rows.get(1).getDepartmentId());
        assertEquals(2, rows.get(1).getEmployeeCount());
        assertEquals(3, rows.get(2).getDepartmentId());
        assertEquals(1, rows.get(2).getEmployeeCount());
    }

    @Test
    void emptyDatabaseReturnsEmptyStatistics() {
        assertTrue(departments.countEmployeesByDepartment().isEmpty());
        assertEquals(0, reports.statistics().getTotalEmployees());
    }

    private void seedEmployees() {
        jdbc.update("INSERT INTO department (id, name) VALUES (1, 'IT'), (2, 'HR'), (3, 'IT')");
        jdbc.update("""
                INSERT INTO employee (name, email, department_id)
                VALUES ('An', 'an@example.com', 1),
                       ('Binh', 'binh@example.com', 1),
                       ('Chi', 'chi@example.com', 3)
                """);
    }

    @Test
    void statisticsTotalMatchesDepartmentCounts() {
        seedEmployees();
        var statistics = reports.statistics();
        assertEquals(3, statistics.getTotalEmployees());
        jdbc.update("DELETE FROM employee WHERE department_id = 3");
        assertEquals(2, reports.statistics().getTotalEmployees());
    }

    @Test
    void userAndAdminCanReadStatisticsApi() throws Exception {
        seedEmployees();
        for (String role : new String[] {"USER", "ADMIN"}) {
            mvc.perform(get("/api/employees/statistics").with(user("reporter").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalEmployees").value(3))
                    .andExpect(jsonPath("$.departments.length()").value(3))
                    .andExpect(jsonPath("$.departments[0].departmentName").value("HR"))
                    .andExpect(jsonPath("$.departments[0].employeeCount").value(0));
        }
    }

    @Test
    void anonymousCannotReadStatisticsApi() throws Exception {
        mvc.perform(get("/api/employees/statistics")).andExpect(status().isUnauthorized());
    }


    @Test
    void userAndAdminCanViewStatisticsPage() throws Exception {
        seedEmployees();
        for (String role : new String[] {"USER", "ADMIN"}) {
            mvc.perform(get("/employees/statistics").with(user("reporter").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/statistics"))
                    .andExpect(model().attributeExists("statistics"))
                    .andExpect(content().contentTypeCompatibleWith("text/html"))
                    .andExpect(content().string(containsString("<strong>3</strong>")))
                    .andExpect(content().string(containsString("HR")))
                    .andExpect(content().string(containsString("IT")))
                    .andExpect(content().string(containsString("<td>0</td>")));
        }
    }

    @Test
    void emptyPageShowsZeroAndAnonymousIsRejected() throws Exception {
        mvc.perform(get("/employees/statistics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/employees/statistics").with(user("reader").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<strong>0</strong>")))
                .andExpect(content().string(containsString("Chưa có phòng ban.")));
    }

}
