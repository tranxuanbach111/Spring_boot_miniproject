package com.example.employeemanagement.dto;

import java.util.List;

public class EmployeeStatistics {

    private final long totalEmployees;
    private final List<DepartmentStatistics> departments;

    public EmployeeStatistics(List<DepartmentStatistics> departments) {
        this.departments = List.copyOf(departments);
        this.totalEmployees = departments.stream().mapToLong(DepartmentStatistics::getEmployeeCount).sum();
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public List<DepartmentStatistics> getDepartments() {
        return departments;
    }
}
