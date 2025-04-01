package com.github.msemitkin.financie.mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static com.github.msemitkin.financie.mono.SignUtil.generateXSign;

@Component
public class MonobankIntegration {
    private static final Logger logger = LoggerFactory.getLogger(MonobankIntegration.class);

    private static final String PERSONAL_AUTH_REQUEST_URL = "/personal/auth/request";
    private static final String MONOBANK_API_ROOT = "https://api.monobank.ua";
    private static final String MONOBANK_AUTH_URI = MONOBANK_API_ROOT + PERSONAL_AUTH_REQUEST_URL;

    private final String keyId;
    private final String pathToPkcs8PrivateKeyPem;

    public MonobankIntegration(
        @Value("${monobank.key-id}") String keyId,
        @Value("${monobank.private-pkcs8-key-pem-path}") String pathToPkcs8PrivateKeyPem
    ) {
        this.keyId = keyId;
        this.pathToPkcs8PrivateKeyPem = pathToPkcs8PrivateKeyPem;
    }

    public RequestAuthResponse requestAuth(String authCallbackUrl) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        var xSign = generateXSign(pathToPkcs8PrivateKeyPem, timestamp, PERSONAL_AUTH_REQUEST_URL);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MONOBANK_AUTH_URI))
            .header("X-Key-Id", keyId)
            .header("X-Time", timestamp)
            .header("X-Sign", xSign)
            .header("X-Callback", authCallbackUrl)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            RequestAuthResponse requestAuthResponse = objectMapper.readValue(response.body(), RequestAuthResponse.class);

            if (response.statusCode() == 200) {
                logger.info("Authorization request initiated successfully.");
                logger.atInfo().log(() -> "Response: " + response.body());
                return requestAuthResponse;
            } else {
                logger.error("Failed to initiate authorization request. Status code: {}", response.statusCode());
                logger.atError().log(() -> "Response: " + response.body());
                throw new RuntimeException("Failed to initiate authorization request. Status code: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Error during authorization request", e);
        }
    }

    public void registerWebHook(String requestId, String userWebHookUrl) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        var xSign = generateXSign(pathToPkcs8PrivateKeyPem, timestamp, "/personal/corp/webhook");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MONOBANK_API_ROOT + "/personal/corp/webhook"))
            .header("X-Key-Id", keyId)
            .header("X-Time", timestamp)
            .header("X-Sign", xSign)
            .header("X-Request-Id", requestId)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"webHookUrl\": \"" + userWebHookUrl + "\"}"))
            .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Webhook successfully registered in Monobank");
            } else {
                throw new RuntimeException("Failed to initiate webhook. Status code: " + response.statusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
