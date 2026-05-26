package com.microservicios.api_gateway.service;

import reactor.core.publisher.Mono;

public interface TokenValidator {
    Mono<Boolean> isValid(String token);
}