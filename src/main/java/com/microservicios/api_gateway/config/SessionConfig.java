package com.microservicios.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.ReactiveRedisSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;


@Configuration
public class SessionConfig {

    /**
     * Este bean configura el serializador para los atributos de la sesión en Redis.
     * Cambiamos a JdkSerializationRedisSerializer para que sea compatible con ms-login,
     * que usa la serialización binaria de Java por defecto.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }

    /**
     * Configura ReactiveRedisOperations para Spring Session.
     * Esto asegura que el ReactiveRedisTemplate interno de Spring Session utilice
     * el JdkSerializationRedisSerializer para la serialización/deserialización
     * de los atributos de sesión, haciéndolo compatible con ms-login.
     */
    @Bean
    public ReactiveRedisOperations<String, Object> sessionRedisOperations(ReactiveRedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializer<Object> valueSerializer = springSessionDefaultRedisSerializer(); // Usa nuestro serializador JSON

        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer) // Las claves dentro del hash de la sesión (ej. "sessionAttr:accessToken")
                .hashValue(valueSerializer) // Los valores de los atributos de la sesión
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public ReactiveRedisSessionRepository reactiveSessionRepository(ReactiveRedisOperations<String, Object> sessionRedisOperations) {
        ReactiveRedisSessionRepository sessionRepository = new ReactiveRedisSessionRepository(sessionRedisOperations);
        // Opcional: configurar prefijos u otros ajustes si fuera necesario
        // sessionRepository.setRedisKeyPrefix(AuthenticationConstants.SPRING_SESSION_KEY_PREFIX);
        return sessionRepository;
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
