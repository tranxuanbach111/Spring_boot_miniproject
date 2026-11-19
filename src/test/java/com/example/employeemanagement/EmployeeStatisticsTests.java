package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.employeemanagement.repository.DepartmentRepository;
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
}
