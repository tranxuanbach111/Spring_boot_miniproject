package com.example.employeemanagement.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class UtilityService {

    private final Pattern whitespacePattern;

    public UtilityService(Pattern whitespacePattern) {
        this.whitespacePattern = whitespacePattern;
    }

    public String normalizeName(String name) {
        return whitespacePattern.matcher(name).replaceAll(" ").strip();
    }
}
