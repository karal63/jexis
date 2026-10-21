package com.jexis.jexis_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@SpringBootApplication
@RestController
public class JexisBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(JexisBackendApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        return "Health check passed!";
    }

    @PostMapping("/create")
    public String create(@Valid @RequestBody TestDto body) {
        return body.getEmail();
    }
}