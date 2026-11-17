package com.example.employeemanagement.dto;

public class EmployeeResponse {

    private final long id;
    private final String name;
    private final String email;
    private final long departmentId;

    public EmployeeResponse(long id, String name, String email, long departmentId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getDepartmentId() {
        return departmentId;
    }
}
