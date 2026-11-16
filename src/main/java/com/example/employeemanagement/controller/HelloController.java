package com.example.employeemanagement.controller;

import com.example.employeemanagement.service.UtilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final UtilityService utilityService;

    public HelloController(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(name = "name", defaultValue = "Employee Management") String name) {
        return "Hello, " + utilityService.normalizeName(name) + "!";
    }
}
