package com.example.employeemanagement.service;

import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employees;

    public EmployeeReportService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Cacheable(cacheNames = "employeeCount", sync = true)
    public long countEmployees() {
        return employees.count();
    }
}
