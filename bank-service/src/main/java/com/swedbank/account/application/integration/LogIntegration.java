package com.swedbank.account.application.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class LogIntegration {

    private final String BASE_URL = "https://tools-httpstatus.pickup-services.com";

    private final RestClient.Builder restClientBuilder;

    public String logSimulatorCall() {
        return restClientBuilder
                .build()
                .get()
                .uri(BASE_URL + "/201")
                .retrieve()
                .toEntity(String.class)
                .getBody();
    }

}
