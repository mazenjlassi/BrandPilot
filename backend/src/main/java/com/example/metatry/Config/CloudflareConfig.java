package com.example.metatry.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudflareConfig {

    @Value("${cloudflare.api.token}")
    private String apiToken;

    @Value("${cloudflare.account.id}")
    private String accountId;

    public String getApiToken() {
        return apiToken;
    }

    public String getAccountId() {
        return accountId;
    }
}
