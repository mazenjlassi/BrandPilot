package com.example.metatry.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XConfig {

    @Value("${x.api-key}")
    private String apiKey;

    @Value("${x.api-key-secret}")
    private String apiKeySecret;

    @Value("${x.access-token}")
    private String accessToken;

    @Value("${x.access-token-secret}")
    private String accessTokenSecret;

    @Bean
    public XCredentials xCredentials() {
        return new XCredentials(apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    public record XCredentials(String apiKey, String apiSecret, String accessToken, String accessTokenSecret) {}
}