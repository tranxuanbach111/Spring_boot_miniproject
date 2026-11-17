package com.example.employeemanagement.controller;

import java.net.URI;
import java.util.List;

import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> findAll(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "department", required = false) String department) {
        return employeeService.search(name, department).stream().map(EmployeeController::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable("id") long id) {
        return ResponseEntity.of(employeeService.findById(id).map(EmployeeController::toResponse));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeService.create(request);
        return ResponseEntity.created(URI.create("/api/employees/" + employee.getId()))
                .body(toResponse(employee));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable("id") long id, @Valid @RequestBody CreateEmployeeRequest request) {
        return toResponse(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getName(), employee.getEmail(),
                employee.getDepartment().getId());
    }
}
