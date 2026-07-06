package com.example.metatry.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LinkedInConfig {

    @Value("${linkedin.client-id}")
    private String clientId;

    @Value("${linkedin.client-secret}")
    private String clientSecret;

    @Value("${linkedin.redirect-uri}")
    private String redirectUri;

    @Bean
    public LinkedInAuthProperties linkedinAuthProperties() {
        return new LinkedInAuthProperties(clientId, clientSecret, redirectUri);
    }

    public record LinkedInAuthProperties(String clientId, String clientSecret, String redirectUri) {}
}