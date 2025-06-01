package com.example.demo.bot;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.bot.bots.BotStrategy;
import com.example.demo.bot.bots.IntelligentBot;
import com.example.demo.bot.bots.SimpleBot;

/**
 * Configuración para los servicios relacionados con el bot.
 */
@Configuration
public class BotConfig {

    @Bean
    @Qualifier("simpleBot")
    public BotStrategy simpleBot() {
        return new SimpleBot();
    }

    @Bean
    @Qualifier("intelligentBot")
    public BotStrategy intelligentBot() {
        return new IntelligentBot();
    }
}
