package com.example.metatry.Services;

import com.example.metatry.Config.XConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class XService {

    private final OAuth10aService service;
    private final OAuth1AccessToken accessToken;
    private final ObjectMapper objectMapper;

    public XService(XConfig.XCredentials credentials) {

        this.service = new ServiceBuilder(credentials.apiKey())
                .apiSecret(credentials.apiSecret())
                .build(TwitterApi.instance());

        this.accessToken = new OAuth1AccessToken(
                credentials.accessToken(),
                credentials.accessTokenSecret()
        );

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Post a text tweet using v1.1 endpoint (OAuth 1.0a compatible)
     */
    public Map<String, Object> postText(String text) {

        try {

            String url = "https://api.twitter.com/1.1/statuses/update.json";

            OAuthRequest request = new OAuthRequest(Verb.POST, url);

            // v1.1 uses form parameters (NOT JSON)
            request.addBodyParameter("status", text);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            int code = response.getCode();
            String body = response.getBody();

            System.out.println("X API Response Code: " + code);
            System.out.println("X API Response Body: " + body);

            if (code == 200) {

                JsonNode json = objectMapper.readTree(body);
                String tweetId = json.path("id_str").asText();

                return Map.of(
                        "success", true,
                        "tweetId", tweetId,
                        "message", "Tweet posted successfully (v1.1)"
                );

            } else {

                return Map.of(
                        "success", false,
                        "error", "Code: " + code + ", Body: " + body
                );
            }

        } catch (Exception e) {

            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Test authentication (GET users/me)
     */
    public Map<String, Object> test() {

        try {

            String url = "https://api.twitter.com/2/users/me";

            OAuthRequest request = new OAuthRequest(Verb.GET, url);
            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return Map.of(
                    "success", true,
                    "message", "Authentication OK",
                    "code", response.getCode()
            );

        } catch (Exception e) {

            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * API Limits info
     */
    public Map<String, Object> getLimits() {

        return Map.of(
                "success", true,
                "maxTweetLength", 280,
                "postingEndpoint", "v1.1/statuses/update.json",
                "authentication", "OAuth 1.0a"
        );
    }

    /**
     * Health check
     */
    public Map<String, Object> health() {

        return Map.of(
                "status", "UP",
                "service", "X Service (v1.1 + OAuth1.0a)",
                "timestamp", System.currentTimeMillis()
        );
    }
}