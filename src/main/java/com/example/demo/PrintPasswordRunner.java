package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class PrintPasswordRunner implements CommandLineRunner {

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public void run(String... args) {
        System.out.println("🟣 DB PASSWORD (from env or properties): " + dbPassword);
    }
}
