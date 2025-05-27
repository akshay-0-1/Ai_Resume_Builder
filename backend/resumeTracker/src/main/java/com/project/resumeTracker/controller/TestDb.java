package com.project.resumeTracker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestDb {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db")
    public String testDbConnection() {
        try {
            String result = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            return "Successfully connected to database: " + result;
        } catch (Exception e) {
            return "Failed to connect to database: " + e.getMessage();
        }
    }
}

