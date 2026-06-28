package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.FacebookService;
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
class FacebookServiceIntegrationTest {

    @Autowired
    private FacebookService facebookService;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Test
    void postText_returnsResponse() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "fb-post-1"));

        ReflectionTestUtils.setField(facebookService, "pageId", "123");
        ReflectionTestUtils.setField(facebookService, "token", "fake-token");

        Map<String, Object> result = facebookService.postText("Hello");

        assertThat(result.get("id")).isEqualTo("fb-post-1");
    }

    @Test
    void postPhotoFromUrl_returnsResponse() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "fb-photo-1"));

        ReflectionTestUtils.setField(facebookService, "pageId", "123");
        ReflectionTestUtils.setField(facebookService, "token", "fake-token");

        Map<String, Object> result = facebookService.postPhotoFromUrl("http://img.com/a.jpg", "Caption");

        assertThat(result.get("id")).isEqualTo("fb-photo-1");
    }

    @Test
    void postVideoFromUrl_returnsResponse() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "fb-video-1"));

        ReflectionTestUtils.setField(facebookService, "pageId", "123");
        ReflectionTestUtils.setField(facebookService, "token", "fake-token");

        Map<String, Object> result = facebookService.postVideoFromUrl("http://video.com/v.mp4", "Desc");

        assertThat(result.get("id")).isEqualTo("fb-video-1");
    }

    @Test
    void postLocalPhoto_uploadsThenPosts() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenReturn("http://cloudinary.com/img.jpg");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("id", "fb-local-1"));

        ReflectionTestUtils.setField(facebookService, "pageId", "123");
        ReflectionTestUtils.setField(facebookService, "token", "fake-token");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "content".getBytes());

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Caption");

        assertThat(result.get("id")).isEqualTo("fb-local-1");
    }

    @Test
    void postLocalPhoto_cloudinaryFails_returnsError() throws Exception {
        when(cloudinaryService.uploadImage(any())).thenThrow(new RuntimeException("Upload failed"));

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "content".getBytes());

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Caption");

        assertThat(result.get("success")).isEqualTo(false);
    }

    // @Test
    // void postMultiplePhotos_returnsResponse() {
    //     when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
    //             .thenReturn(Map.of("id", "media-1"));
    //
    //     when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
    //             .thenReturn(new org.springframework.http.ResponseEntity<>(
    //                     Map.of("id", "fb-album-1"),
    //                     org.springframework.http.HttpStatus.OK));
    //
    //     ReflectionTestUtils.setField(facebookService, "pageId", "123");
    //     ReflectionTestUtils.setField(facebookService, "token", "fake-token");
    //
    //     Map<String, Object> result = facebookService.postMultiplePhotos(
    //             List.of("http://img.com/1.jpg", "http://img.com/2.jpg"), "Album");
    //
    //     assertThat(result.get("id")).isEqualTo("fb-album-1");
    // }
}
