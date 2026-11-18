package com.example.employeemanagement.controller;

import java.util.List;

import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/employees")
public class EmployeeWebController {

    private final EmployeeService employees;
    private final DepartmentRepository departments;

    public EmployeeWebController(EmployeeService employees, DepartmentRepository departments) {
        this.employees = employees;
        this.departments = departments;
    }

    @ModelAttribute("departments")
    public List<Department> departments() {
        return departments.findAll();
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("employees", employees.findAll());
        return "employees/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new CreateEmployeeRequest());
        return "employees/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("employee") CreateEmployeeRequest employee,
                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "employees/add";
        }
        try {
            employees.create(employee);
        } catch (ResponseStatusException exception) {
            if (!exception.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                throw exception;
            }
            bindingResult.rejectValue("departmentId", "department.notFound", "Phòng ban không tồn tại.");
            return "employees/add";
        }
        return "redirect:/employees/list";
    }
}
