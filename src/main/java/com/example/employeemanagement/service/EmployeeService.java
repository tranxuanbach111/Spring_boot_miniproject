package com.example.employeemanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.example.employeemanagement.model.Employee;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final List<Employee> employees = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<Employee> findAll(String department) {
        return employees.stream()
                .filter(employee -> department == null
                        || department.equalsIgnoreCase(employee.getDepartment()))
                .toList();
    }

    public Optional<Employee> findById(long id) {
        return employees.stream().filter(employee -> employee.getId() == id).findFirst();
    }

    public Employee create(String name, String department) {
        Employee employee = new Employee(nextId.getAndIncrement(), name, department);
        employees.add(employee);
        return employee;
    }
}
