package com.example.employeemanagement.repository;

import java.util.List;

import com.example.employeemanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByDepartment_NameIgnoreCase(String department);

    List<Employee> findByNameContainingIgnoreCaseAndDepartment_NameIgnoreCase(String name, String department);
}
