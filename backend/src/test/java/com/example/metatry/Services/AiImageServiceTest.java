package com.example.metatry.Services;

import com.example.metatry.Config.CloudflareConfig;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiImageServiceTest {

    @Mock private CloudinaryService cloudinaryService;
    @Mock private CloudflareConfig cloudflareConfig;
    @Mock private PostImageRepository postImageRepository;

    @Captor private ArgumentCaptor<PostImage> imageCaptor;

    private RestTemplate restTemplate;
    private AiImageService aiImageService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        aiImageService = new AiImageService(cloudinaryService, cloudflareConfig, postImageRepository);
        ReflectionTestUtils.setField(aiImageService, "restTemplate", restTemplate);
    }

    private void stubCfOk() {
        ResponseEntity<byte[]> cfResponse = new ResponseEntity<>("img".getBytes(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class))).thenReturn(cfResponse);
    }

    // ================= generateImageForPost =================

    @Test
    void generateImageForPost_withNullPost_throwsBadRequest() throws Exception {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateImageForPost(null));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateImageForPost_withNoImage_createsNew() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("https://res.cloudinary.com/test.png");

        Post post = Post.builder()
                .id(1L)
                .title("Test Title Marketing")
                .content("Great content about marketing strategies")
                .platform(PlatformType.LINKEDIN)
                .images(null)
                .build();

        PostImage savedImage = PostImage.builder()
                .id(1L)
                .imageUrl("https://res.cloudinary.com/test.png")
                .imagePrompt("prompt")
                .size(ImageSize.LANDSCAPE)
                .post(post)
                .selected(true)
                .build();
        when(postImageRepository.save(any())).thenReturn(savedImage);

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getImageUrl()).isEqualTo("https://res.cloudinary.com/test.png");
        assertThat(result.getSelected()).isTrue();
        verify(postImageRepository).save(imageCaptor.capture());
        PostImage captured = imageCaptor.getValue();
        assertThat(captured.getImagePrompt()).contains("professional cinematic lighting");
    }

    @Test
    void generateImageForPost_withExistingImage_regenerates() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("https://res.cloudinary.com/new.png");
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage existingImage = PostImage.builder()
                .id(1L)
                .imagePrompt("existing prompt")
                .size(ImageSize.SQUARE)
                .build();

        Post post = Post.builder()
                .id(2L)
                .title("Title")
                .content("Content")
                .platform(PlatformType.INSTAGRAM)
                .images(List.of(existingImage))
                .build();

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getImageUrl()).isEqualTo("https://res.cloudinary.com/new.png");
        assertThat(result.getSelected()).isTrue();
    }

    @Test
    void generateImageForPost_withExistingImageAndNullPrompt_rebuildsPrompt() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("https://res.cloudinary.com/new.png");
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage existingImage = PostImage.builder()
                .id(1L)
                .imagePrompt(null)
                .size(ImageSize.LANDSCAPE)
                .build();

        Post post = Post.builder()
                .id(3L)
                .title("AI Technology")
                .content("Latest AI trends")
                .platform(PlatformType.FACEBOOK)
                .images(List.of(existingImage))
                .build();

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getImagePrompt()).contains("technology");
    }

    // ================= generateAndUploadImage =================

    @Test
    void generateAndUploadImage_success_returnsUrl() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("https://res.cloudinary.com/result.png");

        String result = aiImageService.generateAndUploadImage("test prompt", ImageSize.SQUARE);

        assertThat(result).isEqualTo("https://res.cloudinary.com/result.png");
    }

    @Test
    void generateAndUploadImage_cloudflareNon200_throws() {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");

        ResponseEntity<byte[]> cfResponse = new ResponseEntity<>("error".getBytes(), HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class))).thenReturn(cfResponse);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateAndUploadImage_emptyBytes_throws() {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");

        ResponseEntity<byte[]> cfResponse = new ResponseEntity<>(new byte[0], HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class))).thenReturn(cfResponse);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateAndUploadImage("test", ImageSize.PORTRAIT));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateAndUploadImage_nullBytes_throws() {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");

        ResponseEntity<byte[]> cfResponse = new ResponseEntity<>((byte[]) null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class))).thenReturn(cfResponse);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateAndUploadImage_cloudinaryFails_throws() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateAndUploadImage_cloudinaryBlank_throws() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void generateAndUploadImage_truncatesLongPrompt() throws Exception {
        String longPrompt = "a".repeat(600);
        when(cloudflareConfig.getAccountId()).thenReturn("cf-account");
        when(cloudflareConfig.getApiToken()).thenReturn("cf-token");
        stubCfOk();
        when(cloudinaryService.uploadImageBytes(any())).thenReturn("https://res.cloudinary.com/img.png");

        String result = aiImageService.generateAndUploadImage(longPrompt, ImageSize.SQUARE);
        assertThat(result).isEqualTo("https://res.cloudinary.com/img.png");
    }

    // ================= imageSizeForPlatform =================

    @Test
    void imageSizeForPlatform_instagram_returnsSquare() {
        ImageSize result = ReflectionTestUtils.invokeMethod(
                aiImageService, "imageSizeForPlatform", PlatformType.INSTAGRAM);
        assertThat(result).isEqualTo(ImageSize.SQUARE);
    }

    @Test
    void imageSizeForPlatform_linkedin_returnsLandscape() {
        ImageSize result = ReflectionTestUtils.invokeMethod(
                aiImageService, "imageSizeForPlatform", PlatformType.LINKEDIN);
        assertThat(result).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void imageSizeForPlatform_facebook_returnsLandscape() {
        ImageSize result = ReflectionTestUtils.invokeMethod(
                aiImageService, "imageSizeForPlatform", PlatformType.FACEBOOK);
        assertThat(result).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void imageSizeForPlatform_null_returnsSquare() {
        ImageSize result = ReflectionTestUtils.invokeMethod(
                aiImageService, "imageSizeForPlatform", (PlatformType) null);
        assertThat(result).isEqualTo(ImageSize.SQUARE);
    }

    // ================= buildPrompt =================

    @Test
    void buildPrompt_withExistingImagePrompt_returnsIt() {
        PostImage image = PostImage.builder()
                .imagePrompt("custom existing prompt")
                .build();
        Post post = Post.builder()
                .title("Whatever")
                .content("Content")
                .images(List.of(image))
                .build();

        String result = ReflectionTestUtils.invokeMethod(
                aiImageService, "buildPrompt", post, ImageSize.SQUARE);
        assertThat(result).isEqualTo("custom existing prompt");
    }

    @Test
    void buildPrompt_withTitleAndContent_generatesKeywords() {
        Post post = Post.builder()
                .title("Top Marketing Strategies for 2026")
                .content("Learn how to grow your business with digital marketing")
                .images(null)
                .build();

        String result = ReflectionTestUtils.invokeMethod(
                aiImageService, "buildPrompt", post, ImageSize.SQUARE);
        assertThat(result).contains("square 1:1");
        assertThat(result).contains("professional cinematic lighting");
        assertThat(result).contains("marketing");
        assertThat(result).contains("strategies");
        assertThat(result).contains("digital");
        assertThat(result).contains("business");
    }

    @Test
    void buildPrompt_withOnlyTitle_generatesKeywords() {
        Post post = Post.builder()
                .title("Innovation")
                .content(null)
                .images(null)
                .build();

        String result = ReflectionTestUtils.invokeMethod(
                aiImageService, "buildPrompt", post, ImageSize.LANDSCAPE);
        assertThat(result).contains("landscape 16:9");
        assertThat(result).contains("innovation");
    }

    @Test
    void buildPrompt_filtersStopWords() {
        Post post = Post.builder()
                .title("The best and the latest new post title")
                .content("This is just a test content here")
                .images(null)
                .build();

        String result = ReflectionTestUtils.invokeMethod(
                aiImageService, "buildPrompt", post, ImageSize.PORTRAIT);
        assertThat(result).contains("portrait 9:16");
        assertThat(result).doesNotContain("the");
        assertThat(result).doesNotContain("and");
        assertThat(result).doesNotContain("this");
    }
}
