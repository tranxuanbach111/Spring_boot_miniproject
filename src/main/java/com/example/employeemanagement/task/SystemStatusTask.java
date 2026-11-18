package com.example.employeemanagement.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SystemStatusTask {

    private static final Logger log = LoggerFactory.getLogger(SystemStatusTask.class);

    @Scheduled(fixedRate = 30000)
    public void logStatus() {
        log.info("System running");
    }
}
