package com.scalemesh.gateway.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public RestTemplate gatewayRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(10000))
                .errorHandler(new PassThroughResponseErrorHandler())
                .build();
    }

    /**
     * Preserves downstream HTTP error status codes (e.g. 400, 404, 409) so Gateway can
     * transparently stream exact downstream responses back to the client.
     */
    private static class PassThroughResponseErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return false; // Treat all HTTP response codes as readable without throwing
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // No-op
        }
    }
}
