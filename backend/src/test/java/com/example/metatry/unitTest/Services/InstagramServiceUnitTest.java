package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstagramServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CloudinaryService cloudinaryService;

    private InstagramService instagramService;

    @BeforeEach
    void setUp() {
        instagramService = new InstagramService(restTemplate, cloudinaryService);
        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");
    }

    @Test
    void postPhotoFromUrl_returnsSuccess() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-123"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-456"));

        Map<String, Object> result = instagramService.postPhotoFromUrl("https://img.url", "Nice photo");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("mediaId")).isEqualTo("media-456");
    }

    @Test
    void postPhotoFromUrl_returnsError_onException() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        Map<String, Object> result = instagramService.postPhotoFromUrl("https://img.url", "Caption");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Upload failed");
    }

    @Test
    void postLocalPhoto_returnsSuccess() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenReturn("https://cloudinary.com/img.jpg");
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-789"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-101"));

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Local photo");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postLocalPhoto_returnsError_onUploadFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenThrow(new RuntimeException("Cloudinary error"));

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Failed upload");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Cloudinary error");
    }
}
