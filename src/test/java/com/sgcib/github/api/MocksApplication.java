package com.sgcib.github.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile("test")
@SpringBootApplication
public class MocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(MocksApplication.class, args);
    }
}