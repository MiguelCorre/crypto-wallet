package com.crypto_wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// This configuration class sets up a WebClient bean for making HTTP requests
@Configuration
public class WebClientConfig {
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
