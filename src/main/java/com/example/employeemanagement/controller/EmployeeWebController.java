package com.example.employeemanagement.controller;

import com.example.employeemanagement.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employees")
public class EmployeeWebController {

    private final EmployeeService employees;

    public EmployeeWebController(EmployeeService employees) {
        this.employees = employees;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("employees", employees.findAll());
        return "employees/list";
    }
}
