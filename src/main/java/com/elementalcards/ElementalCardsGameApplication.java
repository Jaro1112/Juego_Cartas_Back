package com.elementalcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElementalCardsGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElementalCardsGameApplication.class, args);
    }
}
