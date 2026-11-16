package com.example.employeemanagement.config;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilityConfig {

    @Bean
    public Pattern whitespacePattern() {
        return Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    }
}
