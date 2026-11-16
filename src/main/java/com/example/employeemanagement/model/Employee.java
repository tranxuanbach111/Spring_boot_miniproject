package com.example.employeemanagement.model;

public class Employee {

    private final long id;
    private final String name;
    private final String department;

    public Employee(long id, String name, String department) {
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
