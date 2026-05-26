package com.microservicios.api_gateway.repository;

import com.microservicios.api_gateway.constants.AuthenticationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Repository
public class RedisSessionRepository implements SessionRepository {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionRepository.class);
    
    // Inyectamos Operations<String, Object> en lugar de Template<String, String>
    private final ReactiveRedisOperations<String, Object> redisOperations;

    public RedisSessionRepository(ReactiveRedisOperations<String, Object> redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Override
    public Mono<String> getAccessToken(String sessionId) {
        String springSessionKey = buildSessionKey(sessionId);
        // El StringCleaner ya no es necesario gracias a Jackson2JsonRedisSerializer
        return redisOperations.opsForHash()
                .get(springSessionKey, AuthenticationConstants.SESSION_ATTR_ACCESS_TOKEN)
                .map(Object::toString) 
                .doOnNext(token -> log.info("Redis devolvió token limpio para sesión={}", sessionId));
    }

    @Override
    public Mono<String> getRefreshToken(String sessionId) {
        String springSessionKey = buildSessionKey(sessionId);
        return redisOperations.opsForHash()
                .get(springSessionKey, AuthenticationConstants.SESSION_ATTR_REFRESH_TOKEN)
                .map(Object::toString)
                .doOnNext(token -> log.info("Redis devolvió refresh token limpio para sesión={}", sessionId));
    }

    /**
     * Opcional pero recomendado para performance: 
     * Método para traer ambos tokens en una sola llamada a la BD.
     */
    public Mono<TokenPair> getBothTokens(String sessionId) {
        String springSessionKey = buildSessionKey(sessionId);
        
        return redisOperations.opsForHash()
                .multiGet(springSessionKey, Arrays.asList(
                        AuthenticationConstants.SESSION_ATTR_ACCESS_TOKEN,
                        AuthenticationConstants.SESSION_ATTR_REFRESH_TOKEN
                ))
                .map(values -> {
                    String access = values.get(0) != null ? values.get(0).toString() : null;
                    String refresh = values.get(1) != null ? values.get(1).toString() : null;
                    return new TokenPair(access, refresh);
                });
    }

    private String buildSessionKey(String sessionId) {
        return AuthenticationConstants.SPRING_SESSION_KEY_PREFIX + sessionId;
    }
    
    // Record auxiliar para devolver ambos tokens juntos
    public record TokenPair(String accessToken, String refreshToken) {}
}