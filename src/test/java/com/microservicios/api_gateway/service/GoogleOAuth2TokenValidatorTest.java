package com.microservicios.api_gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2TokenValidatorTest {

    @Mock
    private WebClient.Builder webClientBuilderMock;

    @Mock
    private WebClient webClientMock;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    private GoogleOAuth2TokenValidator validator;

    @BeforeEach
    void setUp() {
        when(webClientBuilderMock.build()).thenReturn(webClientMock);
        validator = new GoogleOAuth2TokenValidator(webClientBuilderMock);
    }

    @Test
    void isValid_NullToken_ReturnsFalse() {
        StepVerifier.create(validator.isValid(null))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValid_EmptyToken_ReturnsFalse() {
        StepVerifier.create(validator.isValid("   "))
                .expectNext(false)
                .verifyComplete();
    }
}