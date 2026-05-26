package com.microservicios.api_gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GoogleOAuth2TokenValidator implements TokenValidator {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2TokenValidator.class);
    
    // URL oficial de Google para validar Access Tokens
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?access_token=";
    
    private final WebClient webClient;

    public GoogleOAuth2TokenValidator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Boolean> isValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.just(false);
        }

        return webClient.get()
                .uri(GOOGLE_TOKEN_INFO_URL + token)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> response != null && response.contains("exp"))
                .onErrorResume(e -> {
                    log.warn("El token de Google fue rechazado o está expirado: {}", e.getMessage());
                    return Mono.just(false);
                });
    }
}