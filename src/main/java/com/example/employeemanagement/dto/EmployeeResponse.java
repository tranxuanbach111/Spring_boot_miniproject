package com.example.employeemanagement.dto;

public class EmployeeResponse {

    private final long id;
    private final String name;
    private final String department;

    public EmployeeResponse(long id, String name, String department) {
        this.id = id;
        this.name = name;
        this.department = department;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }
}
