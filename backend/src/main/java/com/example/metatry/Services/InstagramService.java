package com.example.metatry.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
@Service
@RequiredArgsConstructor
public class InstagramService {

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;

    @Value("${facebook.page-access-token:}")
    private String token;

    @Value("${instagram.business-id:}")
    private String igId;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v19.0/";

    public Map<String, Object> postPhotoFromUrl(String imageUrl, String caption) {

        try {
            String createUrl = GRAPH_API_URL + igId + "/media";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image_url", imageUrl);
            body.add("caption", caption != null ? caption : "");
            body.add("access_token", token);

            Map createRes = restTemplate.postForObject(createUrl, body, Map.class);

            String creationId = (String) createRes.get("id");

            Thread.sleep(5000);

            String publishUrl = GRAPH_API_URL + igId + "/media_publish";

            MultiValueMap<String, String> publishBody = new LinkedMultiValueMap<>();
            publishBody.add("creation_id", creationId);
            publishBody.add("access_token", token);

            Map publishRes = restTemplate.postForObject(publishUrl, publishBody, Map.class);

            return Map.of(
                    "success", true,
                    "mediaId", publishRes.get("id")
            );

        } catch (Exception e) {
            System.out.println("❌ Instagram postPhotoFromUrl failed: " + e.getMessage());
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> postLocalPhoto(MultipartFile file, String caption) {

        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return postPhotoFromUrl(imageUrl, caption);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> postVideoFromUrl(String videoUrl, String caption) {

        try {
            String createUrl = GRAPH_API_URL + igId + "/media";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("media_type", "REELS");
            body.add("video_url", videoUrl);
            body.add("caption", caption != null ? caption : "");
            body.add("access_token", token);

            Map createRes = restTemplate.postForObject(createUrl, body, Map.class);

            String creationId = (String) createRes.get("id");

            // Poll media status until ready
            String statusUrl = GRAPH_API_URL + igId + "/media/" + creationId + "?fields=status_code&access_token=" + token;
            int attempts = 0;
            while (attempts < 20) {
                attempts++;
                Thread.sleep(10000);
                Map statusRes;
                try {
                    statusRes = restTemplate.getForObject(statusUrl, Map.class);
                } catch (Exception e) {
                    System.out.println("Status check failed (attempt " + attempts + "): " + e.getMessage());
                    continue;
                }
                String statusCode = (String) statusRes.get("status_code");
                System.out.println("Media status check " + attempts + "/20: " + statusCode);
                if ("FINISHED".equals(statusCode)) {
                    System.out.println("Media is ready!");
                    break;
                }
                if ("ERROR".equals(statusCode)) {
                    System.out.println("Media processing failed with status ERROR");
                    return Map.of("success", false, "error", "Media processing failed: " + statusRes);
                }
            }

            System.out.println("Attempting publish...");
            String publishUrl = GRAPH_API_URL + igId + "/media_publish";

            MultiValueMap<String, String> publishBody = new LinkedMultiValueMap<>();
            publishBody.add("creation_id", creationId);
            publishBody.add("access_token", token);

            Map publishRes = restTemplate.postForObject(publishUrl, publishBody, Map.class);

            return Map.of(
                    "success", true,
                    "mediaId", publishRes.get("id")
            );

        } catch (Exception e) {
            System.out.println("❌ Instagram postVideoFromUrl failed for URL: " + videoUrl);
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace(System.out);
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
