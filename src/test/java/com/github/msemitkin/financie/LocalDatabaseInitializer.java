package com.github.msemitkin.financie;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.PostgreSQLContainer;

public class LocalDatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres")
            .withDatabaseName("test_database2")
            .withUsername("test")
            .withPassword("test");

    static {
        postgresContainer.start();
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=".concat(postgresContainer.getJdbcUrl()),
            "spring.datasource.username=".concat(postgresContainer.getUsername()),
            "spring.datasource.password=".concat(postgresContainer.getPassword())
        ).applyTo(applicationContext);
    }

}
