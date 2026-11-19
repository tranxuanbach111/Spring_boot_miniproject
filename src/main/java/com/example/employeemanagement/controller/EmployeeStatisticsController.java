package com.example.employeemanagement.controller;

import com.example.employeemanagement.service.EmployeeReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeStatisticsController {

    private final EmployeeReportService reports;

    public EmployeeStatisticsController(EmployeeReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/employees/statistics")
    public String statistics(Model model) {
        model.addAttribute("statistics", reports.statistics());
        return "employees/statistics";
    }
}
