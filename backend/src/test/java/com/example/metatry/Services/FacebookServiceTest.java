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
class FacebookServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CloudinaryService cloudinaryService;

    private FacebookService facebookService;

    @BeforeEach
    void setUp() {
        facebookService = new FacebookService(restTemplate, cloudinaryService);
    }

    @Test
    void postText_returnsResult() {
        Map<String, Object> expected = Map.of("id", "12345");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expected);

        Map<String, Object> result = facebookService.postText("Hello World");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postPhotoFromUrl_returnsResult() {
        Map<String, Object> expected = Map.of("id", "67890");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expected);

        Map<String, Object> result = facebookService.postPhotoFromUrl("https://img.url/photo.jpg", "Caption");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postPhotoFromUrl_withNullCaption_returnsResult() {
        Map<String, Object> expected = Map.of("id", "67890");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expected);

        Map<String, Object> result = facebookService.postPhotoFromUrl("https://img.url/photo.jpg", null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postLocalPhoto_returnsResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenReturn("https://cloudinary.com/img.png");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Map.of("id", "11111"));

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Caption");

        assertThat(result.get("id")).isEqualTo("11111");
    }

    @Test
    void postLocalPhoto_whenCloudinaryFails_returnsError() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenThrow(new RuntimeException("Upload failed"));

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Upload failed");
    }
}
