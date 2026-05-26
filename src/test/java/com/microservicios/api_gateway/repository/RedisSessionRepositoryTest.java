package com.microservicios.api_gateway.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSessionRepositoryTest {

    // Cambiamos ReactiveRedisTemplate<String, String> por ReactiveRedisOperations<String, Object>
    @Mock
    private ReactiveRedisOperations<String, Object> redisOperations;

    @Mock
    private ReactiveHashOperations<String, Object, Object> hashOperations;

    private RedisSessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        when(redisOperations.opsForHash()).thenReturn(hashOperations);
        sessionRepository = new RedisSessionRepository(redisOperations);
    }

    @Test
    void getAccessToken_existingToken_shouldReturnToken() {
        String sessionId = "test-session-123";
        String redisKey = "spring:session:sessions:" + sessionId;
        String token = "ya29.tokenxxx"; // Ya no testeamos comillas porque Redis devuelve objetos JSON limpios

        when(hashOperations.get(redisKey, "sessionAttr:accessToken"))
                .thenReturn(Mono.just(token));

        StepVerifier.create(sessionRepository.getAccessToken(sessionId))
                .expectNext("ya29.tokenxxx")
                .verifyComplete();

        verify(hashOperations).get(redisKey, "sessionAttr:accessToken");
    }

    @Test
    void getAccessToken_notFound_shouldReturnEmpty() {
        String sessionId = "non-existent-session";
        String redisKey = "spring:session:sessions:" + sessionId;

        when(hashOperations.get(redisKey, "sessionAttr:accessToken"))
                .thenReturn(Mono.empty());

        StepVerifier.create(sessionRepository.getAccessToken(sessionId))
                .verifyComplete();
    }

    @Test
    void getRefreshToken_existingToken_shouldReturnToken() {
        String sessionId = "test-session-123";
        String redisKey = "spring:session:sessions:" + sessionId;
        String refreshToken = "1//refresh-token-xxx";

        when(hashOperations.get(redisKey, "sessionAttr:refreshToken"))
                .thenReturn(Mono.just(refreshToken));

        StepVerifier.create(sessionRepository.getRefreshToken(sessionId))
                .expectNext("1//refresh-token-xxx")
                .verifyComplete();
    }

    @Test
    void getRefreshToken_notFound_shouldReturnEmpty() {
        String sessionId = "test-session-123";
        String redisKey = "spring:session:sessions:" + sessionId;

        when(hashOperations.get(redisKey, "sessionAttr:refreshToken"))
                .thenReturn(Mono.empty());

        StepVerifier.create(sessionRepository.getRefreshToken(sessionId))
                .verifyComplete(); // Ahora devuelve Mono.empty() directamente
    }

    @Test
    void getAccessToken_redisError_shouldPropagateError() {
        String sessionId = "test-session-123";
        String redisKey = "spring:session:sessions:" + sessionId;

        when(hashOperations.get(redisKey, "sessionAttr:accessToken"))
                .thenReturn(Mono.error(new RuntimeException("Redis connection failed")));

        StepVerifier.create(sessionRepository.getAccessToken(sessionId))
                .expectErrorMessage("Redis connection failed")
                .verify();
    }
}