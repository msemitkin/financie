package com.github.msemitkin.financie;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ReadinessPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ReadinessPublisher.class);

    @EventListener(ApplicationReadyEvent.class)
    public void publishReadiness() {
        try {
            Path filePath = Path.of("ready");
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
        } catch (IOException e) {
            logger.error("Failed to create readiness file", e);
        }
    }

    @PreDestroy
    public void removeReadiness() {
        try {
            Files.deleteIfExists(Path.of("ready"));
        } catch (IOException e) {
            logger.error("Failed to remove readiness file", e);
        }
    }
}
