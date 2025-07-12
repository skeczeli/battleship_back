package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("🔍 Trying to read SPRING_DATASOURCE_PASSWORD = " +
                System.getenv("SPRING_DATASOURCE_PASSWORD"));
        SpringApplication.run(DemoApplication.class, args);
    }
}