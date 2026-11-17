package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:employee_test;DB_CLOSE_DELAY=-1",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EmployeeManagementApplicationTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmployeeService employees;

    @Test
    void employeeCrudPersistsChanges() {
        jdbc.update("INSERT INTO department (id, name) VALUES (?, ?)", 1L, "IT");
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Nguyen Van An");
        request.setEmail("an@example.com");
        request.setDepartmentId(1L);

        long id = employees.create(request).getId();
        assertEquals(List.of(id), employees.findAll().stream().map(Employee::getId).toList());
        Employee employee = employees.findById(id).orElseThrow();
        assertEquals("an@example.com", employee.getEmail());
        assertEquals(1L, employee.getDepartment().getId());

        request.setEmail("an.updated@example.com");
        employees.update(id, request);
        assertEquals("an.updated@example.com", employees.findById(id).orElseThrow().getEmail());

        employees.delete(id);
        assertTrue(employees.findById(id).isEmpty());
    }
}
