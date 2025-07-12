package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PrintPasswordRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("✅ Backend arrancó y ejecutó código propio.");
    }
}
