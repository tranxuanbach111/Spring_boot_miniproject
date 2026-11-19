package com.example.employeemanagement.dto;

public class DepartmentStatistics {

    private final long departmentId;
    private final String departmentName;
    private final long employeeCount;

    public DepartmentStatistics(long departmentId, String departmentName, long employeeCount) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.employeeCount = employeeCount;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }
}
