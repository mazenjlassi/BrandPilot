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
class FacebookServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CloudinaryService cloudinaryService;

    private FacebookService facebookService;

    @BeforeEach
    void setUp() {
        facebookService = new FacebookService(restTemplate, cloudinaryService);
        ReflectionTestUtils.setField(facebookService, "pageId", "123");
        ReflectionTestUtils.setField(facebookService, "token", "fake-token");
    }

    @Test
    void postText_returnsResponse() {
        Map<String, Object> expected = Map.of("id", "fb-post-1");
        when(restTemplate.postForObject(contains("/feed"), any(), eq(Map.class)))
                .thenReturn(expected);

        Map<String, Object> result = facebookService.postText("Hello Facebook");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postPhotoFromUrl_returnsResponse() {
        Map<String, Object> expected = Map.of("id", "fb-photo-1");
        when(restTemplate.postForObject(contains("/photos"), any(), eq(Map.class)))
                .thenReturn(expected);

        Map<String, Object> result = facebookService.postPhotoFromUrl("https://img.url", "Great photo");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postPhotoFromUrl_handlesNullCaption() {
        Map<String, Object> expected = Map.of("id", "fb-photo-2");
        when(restTemplate.postForObject(contains("/photos"), any(), eq(Map.class)))
                .thenReturn(expected);

        Map<String, Object> result = facebookService.postPhotoFromUrl("https://img.url", null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postLocalPhoto_returnsSuccess() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenReturn("https://cloudinary.com/img.jpg");
        Map<String, Object> expected = Map.of("id", "fb-photo-3");
        when(restTemplate.postForObject(contains("/photos"), any(), eq(Map.class)))
                .thenReturn(expected);

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Local");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void postLocalPhoto_returnsError_onUploadFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(file)).thenThrow(new RuntimeException("Upload error"));

        Map<String, Object> result = facebookService.postLocalPhoto(file, "Fail");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Upload error");
    }
}
