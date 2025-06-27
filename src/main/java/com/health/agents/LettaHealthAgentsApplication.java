package com.health.agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LettaHealthAgentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LettaHealthAgentsApplication.class, args);
    }
} 