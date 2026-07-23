package com.example.metatry.Services;

import com.example.metatry.Config.GroqConfig;
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

    private final GroqConfig groqConfig;
    private final RestTemplate restTemplate;
    private static final AtomicLong lastRateLimitTime = new AtomicLong(0);

    public String generate(String prompt){

        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", "mixtral-8x7b-32768",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqConfig.getApiKey());

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
                    System.out.println("=== AI Rate Limited (429) ===");
                    System.out.println("Response body: " + retryEx.getResponseBodyAsString());
                    throw new GeminiUnavailableException("AI generation is temporarily over capacity. Please try again in a few minutes.");
                }
            }
            System.out.println("=== AI API HTTP Error ===");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Response body: " + e.getResponseBodyAsString());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace(System.out);
            if (e.getStatusCode().is5xxServerError()) {
                throw new GeminiUnavailableException("AI generation service is temporarily unavailable. Please try again later.");
            }
            throw new RuntimeException("AI API failed (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("=== AI API Error ===");
            System.out.println("Type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException("AI API failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String extractText(Map<String, Object> response){

        try {
            var choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in response: " + response);
            }
            var message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e){
            throw new RuntimeException("Failed to extract AI response: " + response);
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