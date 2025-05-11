package com.example.demo.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para los servicios relacionados con el bot.
 */
@Configuration
public class BotConfig {
    
    @Bean
    public Bot botService() {
        return new Bot();
    }
}