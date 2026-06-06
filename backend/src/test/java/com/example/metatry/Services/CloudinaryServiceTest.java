package com.example.metatry.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    @Test
    void uploadImageBytes_returnsSecureUrl() throws Exception {
        Map<String, Object> uploadResult = Map.of("secure_url", "https://res.cloudinary.com/test/image/upload/v1/test.png");
        when(uploader.upload((byte[]) any(), any(Map.class))).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImageBytes("test-image-data".getBytes());

        assertThat(result).isEqualTo("https://res.cloudinary.com/test/image/upload/v1/test.png");
        verify(uploader).upload((byte[]) any(), any(Map.class));
    }

    @Test
    void uploadWithOptions_returnsFullResult() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getBytes()).thenReturn("fake-image-bytes".getBytes());

        Map<String, Object> options = ObjectUtils.asMap("folder", "custom");
        Map<String, Object> expectedResult = Map.of("secure_url", "https://example.com/img.png", "public_id", "abc123");
        when(uploader.upload(any(java.io.File.class), any(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = cloudinaryService.uploadWithOptions(file, options);

        assertThat(result.get("secure_url")).isEqualTo("https://example.com/img.png");
        assertThat(result.get("public_id")).isEqualTo("abc123");
    }
}
