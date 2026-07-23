package com.example.metatry.Services;

import com.example.metatry.Config.GeminiConfig;
import com.example.metatry.Exceptions.GeminiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private static final AtomicLong lastRateLimitTime = new AtomicLong(0);

    public String generate(String prompt){

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key="
                + geminiConfig.getApiKey();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            String rawText = extractText(response.getBody());

            return cleanJson(rawText);

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 429) {
                long now = System.currentTimeMillis();
                long last = lastRateLimitTime.getAndSet(now);
                long waitMs = Math.min(30000, now - last);
                if (waitMs > 0) {
                    try { Thread.sleep(waitMs); } catch (InterruptedException ignored) {}
                }
                try {
                    ResponseEntity<Map> retryResponse = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
                    String rawText = extractText(retryResponse.getBody());
                    return cleanJson(rawText);
                } catch (HttpStatusCodeException retryEx) {
                    System.out.println("=== Gemini API Rate Limited (429) ===");
                    System.out.println("Response body: " + retryEx.getResponseBodyAsString());
                    throw new GeminiUnavailableException("AI generation is temporarily over capacity. Please try again in a few minutes.");
                }
            }
            System.out.println("=== Gemini API HTTP Error ===");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Response body: " + e.getResponseBodyAsString());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace(System.out);
            if (e.getStatusCode().is5xxServerError()) {
                throw new GeminiUnavailableException("AI generation service is temporarily unavailable. Please try again later.");
            }
            throw new RuntimeException("Gemini API failed (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("=== Gemini API Error ===");
            System.out.println("Type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException("Gemini API failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String extractText(Map<String, Object> response){

        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in response: " + response);
            }
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e){
            throw new RuntimeException("Failed to extract Gemini response: " + response);
        }
    }

    private String cleanJson(String text) {

        if(text == null) return "";

        text = text.replace("```json", "");
        text = text.replace("```", "");

        text = text.trim();

        if (text.startsWith("[")) {
            int firstBracket = text.indexOf("[");
            int lastBracket = text.lastIndexOf("]");
            if (firstBracket != -1 && lastBracket != -1) {
                text = text.substring(firstBracket, lastBracket + 1);
            }
        } else {
            int firstBrace = text.indexOf("{");
            int lastBrace = text.lastIndexOf("}");
            if (firstBrace != -1 && lastBrace != -1) {
                text = text.substring(firstBrace, lastBrace + 1);
            }
        }

        return text;
    }
}