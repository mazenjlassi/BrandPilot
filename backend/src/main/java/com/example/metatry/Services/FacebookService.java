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
import java.util.function.Supplier;@Service
@RequiredArgsConstructor
public class FacebookService {

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;

    @Value("${facebook.page-id}")
    private String pageId;

    @Value("${facebook.page-access-token}")
    private String token;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v19.0/";

    public Map<String, Object> postText(String message) {

        String url = GRAPH_API_URL + pageId + "/feed";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("message", message);
        body.add("access_token", token);

        return restTemplate.postForObject(url, body, Map.class);
    }

    public Map<String, Object> postPhotoFromUrl(String imageUrl, String caption) {

        String url = GRAPH_API_URL + pageId + "/photos";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("url", imageUrl);
        body.add("caption", caption != null ? caption : "");
        body.add("access_token", token);

        return restTemplate.postForObject(url, body, Map.class);
    }

    public Map<String, Object> postLocalPhoto(MultipartFile file, String caption) {

        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return postPhotoFromUrl(imageUrl, caption);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}