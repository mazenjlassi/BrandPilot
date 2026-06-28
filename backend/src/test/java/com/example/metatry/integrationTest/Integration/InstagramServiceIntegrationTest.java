package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.InstagramService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class InstagramServiceIntegrationTest {

    @Autowired
    private InstagramService instagramService;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Test
    void postPhotoFromUrl_success() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-1"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-123"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postPhotoFromUrl("http://img.com/a.jpg", "Caption");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("mediaId")).isEqualTo("media-123");
    }

    @Test
    void postPhotoFromUrl_apiFails_returnsError() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postPhotoFromUrl("http://img.com/a.jpg", "Caption");

        assertThat(result.get("success")).isEqualTo(false);
    }

    @Test
    void postLocalPhoto_uploadsThenPosts() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenReturn("http://cloudinary.com/img.jpg");
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "creation-2"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "media-456"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "content".getBytes());

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postLocalPhoto_cloudinaryFails_returnsError() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenThrow(new RuntimeException("Upload failed"));

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "content".getBytes());

        Map<String, Object> result = instagramService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(false);
    }

    @Test
    void postVideoFromUrl_success() throws Exception {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "video-creation-1"));
        when(restTemplate.getForObject(contains("status_code"), eq(Map.class)))
                .thenReturn(Map.of("status_code", "FINISHED"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "video-media-1"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postVideoFromUrl("http://video.com/v.mp4", "Caption");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postVideoFromUrl_mediaError_returnsError() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "video-creation-2"));
        when(restTemplate.getForObject(contains("status_code"), eq(Map.class)))
                .thenReturn(Map.of("status_code", "ERROR"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postVideoFromUrl("http://video.com/v.mp4", "Caption");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).toString().contains("Media processing failed");
    }

    @Test
    void postCarousel_success() {
        when(restTemplate.postForObject(contains("/media"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "child-1"))
                .thenReturn(Map.of("id", "child-2"))
                .thenReturn(Map.of("id", "carousel-1"));
        when(restTemplate.postForObject(contains("/media_publish"), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "carousel-published"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postCarousel(
                List.of("http://img.com/1.jpg", "http://img.com/2.jpg"), "Carousel");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postCarousel_apiFails_returnsError() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Carousel error"));

        ReflectionTestUtils.setField(instagramService, "token", "fake-token");
        ReflectionTestUtils.setField(instagramService, "igId", "ig-biz-1");

        Map<String, Object> result = instagramService.postCarousel(
                List.of("http://img.com/1.jpg"), "Carousel");

        assertThat(result.get("success")).isEqualTo(false);
    }
}
