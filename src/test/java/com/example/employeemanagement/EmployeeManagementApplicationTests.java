package com.example.employeemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.employeemanagement.controller.EmployeeController;
import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.exception.EmployeeExceptionHandler;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:employee_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
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

    @Test
    @Transactional
    void searchFiltersByNameAndDepartment() {
        jdbc.update("INSERT INTO department (id, name) VALUES (?, ?), (?, ?)", 10L, "IT", 11L, "HR");
        jdbc.update("INSERT INTO employee (name, email, department_id) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                "Nguyen Van An", "an@example.com", 10L,
                "Tran Binh", "binh@example.com", 10L,
                "Nguyen Chi", "chi@example.com", 11L);

        assertEquals(3, employees.search(null, null).size());
        assertEquals(3, employees.search(" ", "").size());
        assertEquals(2, employees.search(" nguyen ", null).size());
        assertEquals(2, employees.search(null, "it").size());
        assertEquals(List.of("Nguyen Van An"), employees.search("nguyen", "IT")
                .stream().map(Employee::getName).toList());
        assertTrue(employees.search("missing", "IT").isEmpty());
    }


    @Test
    void employeeApiReturnsClearErrors() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EmployeeController(employees))
                .setControllerAdvice(new EmployeeExceptionHandler())
                .build();

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ", "email":"invalid", "departmentId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name is required."))
                .andExpect(jsonPath("$.errors.email").value("Email must be valid."));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        mvc.perform(get("/api/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Employee ID must be an integer."));

        mvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee 99999 was not found."));

        mvc.perform(put("/api/employees/99999").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"An", "email":"an@example.com", "departmentId":1}
                                """))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/employees/99999")).andExpect(status().isNotFound());
    }

}
