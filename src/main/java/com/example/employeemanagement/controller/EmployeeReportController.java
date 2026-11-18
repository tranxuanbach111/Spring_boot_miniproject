package com.example.employeemanagement.controller;

import java.util.Map;

import com.example.employeemanagement.service.EmployeeReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeReportController {

    private final EmployeeReportService reports;

    public EmployeeReportController(EmployeeReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("totalEmployees", reports.countEmployees());
    }
}
