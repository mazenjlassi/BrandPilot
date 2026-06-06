package com.example.metatry.Services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LinkedInService {

    private final RestTemplate restTemplate;
    private final LinkedInTokenService tokenService;

    public LinkedInService(RestTemplate restTemplate, LinkedInTokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    /**
     * ✅ Post TEXT (WORKING)
     */
    public Map<String, Object> postText(String text) {

        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Not authenticated with LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = buildTextBody(text);

        return executeRequest(url, jsonBody);
    }

    /**
     * ✅ Post IMAGE via ARTICLE (your approach)
     */
    public Map<String, Object> postArticleWithImage(String text, String imageUrl, String title) {

        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Not authenticated with LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = buildArticleBody(text, imageUrl, title);

        return executeRequest(url, jsonBody);
    }

    /**
     * ✅ Post VIDEO via ARTICLE
     */
    public Map<String, Object> postArticleWithVideo(String text, String videoUrl, String title) {

        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Not authenticated with LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = buildArticleBody(text, videoUrl, title);

        return executeRequest(url, jsonBody);
    }

    /**
     * 🔥 CORE REQUEST HANDLER
     */
    private Map<String, Object> executeRequest(String url, String jsonBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.set("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {

            System.out.println("\n➡️ LINKEDIN REQUEST BODY:\n" + jsonBody);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            String postId = response.getHeaders().getFirst("X-RestLi-Id");

            System.out.println("✅ LINKEDIN RESPONSE HEADERS: " + response.getHeaders());

            if (postId == null || postId.isBlank()) {
                throw new RuntimeException("LinkedIn did not return post ID");
            }

            return Map.of(
                    "success", true,
                    "postId", postId,
                    "linkedinUrl", "https://www.linkedin.com/feed/update/" + postId
            );

        } catch (HttpClientErrorException e) {

            System.err.println("❌ LINKEDIN ERROR STATUS: " + e.getStatusCode());
            System.err.println("❌ LINKEDIN ERROR BODY: " + e.getResponseBodyAsString());

            return Map.of(
                    "success", false,
                    "error", e.getResponseBodyAsString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * 🔹 TEXT BODY
     */
    private String buildTextBody(String text) {

        return String.format(
                "{"
                        + "\"author\": \"%s\","
                        + "\"lifecycleState\": \"PUBLISHED\","
                        + "\"specificContent\": {"
                        + "\"com.linkedin.ugc.ShareContent\": {"
                        + "\"shareCommentary\": {\"text\": \"%s\"},"
                        + "\"shareMediaCategory\": \"NONE\""
                        + "}"
                        + "},"
                        + "\"visibility\": {"
                        + "\"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\""
                        + "}"
                        + "}",
                tokenService.getPersonUrn(),
                escape(text)
        );
    }

    /**
     * 🔹 ARTICLE BODY (image/video URL)
     */
    private String buildArticleBody(String text, String url, String title) {

        return String.format(
                "{"
                        + "\"author\": \"%s\","
                        + "\"lifecycleState\": \"PUBLISHED\","
                        + "\"specificContent\": {"
                        + "\"com.linkedin.ugc.ShareContent\": {"
                        + "\"shareCommentary\": {\"text\": \"%s\"},"
                        + "\"shareMediaCategory\": \"ARTICLE\","
                        + "\"media\": [{"
                        + "\"status\": \"READY\","
                        + "\"originalUrl\": \"%s\","
                        + "\"title\": {\"text\": \"%s\"}"
                        + "}]"
                        + "}"
                        + "},"
                        + "\"visibility\": {"
                        + "\"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\""
                        + "}"
                        + "}",
                tokenService.getPersonUrn(),
                escape(text),
                url,
                escape(title)
        );
    }

    /**
     * 🔥 CRITICAL FIX: Proper JSON escaping
     */
    private String escape(String text) {

        if (text == null) return "";

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", " ");
    }

    /**
     * ✅ GET PROFILE
     */
    public Map<String, Object> getUserProfile() {

        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Not authenticated");
        }

        String url = "https://api.linkedin.com/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return Map.of("success", true, "profile", response.getBody());

        } catch (Exception e) {

            return Map.of("success", false, "error", e.getMessage());
        }
    }
}