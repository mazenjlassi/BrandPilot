package com.example.metatry.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstagramServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CloudinaryService cloudinaryService;

    private InstagramService instagramService;

    @BeforeEach
    void setUp() {
        instagramService = new InstagramService(restTemplate, cloudinaryService);
    }

    @Test
    void postPhotoFromUrl_returnsMediaId() throws Exception {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-123"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-456"));

        Map<String, Object> result = instagramService.postPhotoFromUrl("https://img.url/photo.jpg", "Caption");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("mediaId")).isEqualTo("media-456");
    }

    @Test
    void postPhotoFromUrl_withNullCaption_returnsMediaId() throws Exception {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-123"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-789"));

        Map<String, Object> result = instagramService.postPhotoFromUrl("https://img.url/photo.jpg", null);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("mediaId")).isEqualTo("media-789");
    }

    @Test
    void postPhotoFromUrl_whenCreateFails_returnsError() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        Map<String, Object> result = instagramService.postPhotoFromUrl("https://img.url/photo.jpg", "Caption");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("API error");
    }

    @Test
    void postLocalPhoto_returnsResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenReturn("https://cloudinary.com/img.png");
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-123"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-555"));

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("mediaId")).isEqualTo("media-555");
    }

    @Test
    void postLocalPhoto_whenCloudinaryFails_returnsError() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenThrow(new RuntimeException("Upload failed"));

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Upload failed");
    }
}
