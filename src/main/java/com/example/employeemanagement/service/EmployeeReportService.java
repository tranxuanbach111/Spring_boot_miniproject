package com.example.employeemanagement.service;

import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.dto.EmployeeStatistics;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employees;

    private final DepartmentRepository departments;

    public EmployeeReportService(EmployeeRepository employees, DepartmentRepository departments) {
        this.employees = employees;
        this.departments = departments;
    }

    @Cacheable(cacheNames = "employeeCount", sync = true)
    public long countEmployees() {
        return employees.count();
    }

    @Transactional(readOnly = true)
    public EmployeeStatistics statistics() {
        return new EmployeeStatistics(departments.countEmployeesByDepartment());
    }
}
