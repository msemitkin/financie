package com.github.msemitkin.financie;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TestFinancieApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FinancieApplication.class)
            .initializers(new LocalDatabaseInitializer())
            .profiles("dev")
            .run(args);
    }

}
