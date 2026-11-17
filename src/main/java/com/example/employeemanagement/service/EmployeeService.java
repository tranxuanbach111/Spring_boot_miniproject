package com.example.employeemanagement.service;

import java.util.List;
import java.util.Optional;

import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;

    public EmployeeService(EmployeeRepository employees, DepartmentRepository departments) {
        this.employees = employees;
        this.departments = departments;
    }

    public List<Employee> findAll() {
        return employees.findAll();
    }

    public List<Employee> search(String name, String department) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasDepartment = department != null && !department.isBlank();

        if (hasName && hasDepartment) {
            return employees.findByNameContainingIgnoreCaseAndDepartment_NameIgnoreCase(
                    name.strip(), department.strip());
        }
        if (hasName) {
            return employees.findByNameContainingIgnoreCase(name.strip());
        }
        if (hasDepartment) {
            return employees.findByDepartment_NameIgnoreCase(department.strip());
        }
        return findAll();
    }

    public Optional<Employee> findById(long id) {
        return employees.findById(id);
    }

    @Transactional
    public Employee create(CreateEmployeeRequest request) {
        Department department = findDepartment(request.getDepartmentId());
        return employees.save(new Employee(request.getName(), request.getEmail(), department));
    }

    @Transactional
    public Employee update(long id, CreateEmployeeRequest request) {
        Employee employee = getEmployee(id);
        employee.update(request.getName(), request.getEmail(), findDepartment(request.getDepartmentId()));
        return employee;
    }

    @Transactional
    public void delete(long id) {
        employees.delete(getEmployee(id));
    }

    public Employee getEmployee(long id) {
        return findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee " + id + " was not found."));
    }

    private Department findDepartment(long id) {
        return departments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department does not exist"));
    }
}
