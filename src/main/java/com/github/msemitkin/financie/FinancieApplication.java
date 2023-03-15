package com.github.msemitkin.financie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FinancieApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancieApplication.class, args);
    }

}
