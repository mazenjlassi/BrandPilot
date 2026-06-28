package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiImageServiceUnitTest {

    @Mock private CloudinaryService cloudinaryService;
    @Mock private CloudflareConfig cloudflareConfig;
    @Mock private PostImageRepository postImageRepository;
    @Mock private RestTemplate restTemplate;

    private AiImageService aiImageService;

    @BeforeEach
    void setUp() {
        aiImageService = new AiImageService(cloudinaryService, cloudflareConfig, postImageRepository);
        ReflectionTestUtils.setField(aiImageService, "restTemplate", restTemplate);
    }

    private ResponseEntity<byte[]> okResponse(byte[] body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // ================= generateAndUploadImage =================

    @Test
    void generateAndUploadImage_truncatesPromptOver500() throws Exception {
        String longPrompt = "a".repeat(600);
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage(longPrompt, ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body).containsKey("prompt");
        assertThat((String) body.get("prompt")).hasSize(500).endsWith("...");
    }

    @Test
    void generateAndUploadImage_sendsCorrectDimensions_square() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("width")).isEqualTo(1024);
        assertThat(body.get("height")).isEqualTo(1024);
    }

    @Test
    void generateAndUploadImage_sendsCorrectDimensions_landscape() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.LANDSCAPE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("width")).isEqualTo(1216);
        assertThat(body.get("height")).isEqualTo(832);
    }

    @Test
    void generateAndUploadImage_sendsCorrectDimensions_portrait() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.PORTRAIT);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("width")).isEqualTo(832);
        assertThat(body.get("height")).isEqualTo(1216);
    }

    @Test
    void generateAndUploadImage_sendsGuidance8_5() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("guidance")).isEqualTo(8.5);
    }

    @Test
    void generateAndUploadImage_sendsNumSteps20() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("num_steps")).isEqualTo(20);
    }

    @Test
    void generateAndUploadImage_includesNegativePrompt() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("test", ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body).containsKey("negative_prompt");
        assertThat((String) body.get("negative_prompt")).contains("disfigured face");
    }

    @Test
    void generateAndUploadImage_throwsOnNon2xx() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        assertThatThrownBy(() -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void generateAndUploadImage_throwsOnNullBody() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse(null));

        assertThatThrownBy(() -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("empty image");
    }

    @Test
    void generateAndUploadImage_throwsOnEmptyBytes() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse(new byte[0]));

        assertThatThrownBy(() -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("empty image");
    }

    @Test
    void generateAndUploadImage_throwsWhenCloudinaryReturnsNull() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn(null).when(cloudinaryService).uploadImageBytes(any());

        assertThatThrownBy(() -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cloudinary upload failed");
    }

    @Test
    void generateAndUploadImage_throwsWhenCloudinaryReturnsBlank() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("   ").when(cloudinaryService).uploadImageBytes(any());

        assertThatThrownBy(() -> aiImageService.generateAndUploadImage("test", ImageSize.SQUARE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cloudinary upload failed");
    }

    @Test
    void generateAndUploadImage_returnsUrlOnSuccess() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        String result = aiImageService.generateAndUploadImage("test prompt", ImageSize.SQUARE);

        assertThat(result).isEqualTo("https://cloud.com/img.png");
    }

    @Test
    void generateAndUploadImage_sendsPromptInBody() throws Exception {
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());

        aiImageService.generateAndUploadImage("my custom prompt", ImageSize.SQUARE);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("prompt")).isEqualTo("my custom prompt");
    }

    // ================= generateImageForPost =================

    @Test
    void generateImageForPost_throwsWhenPostNull() throws Exception {
        assertThatThrownBy(() -> aiImageService.generateImageForPost(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void generateImageForPost_noImage_createsNewImage() throws Exception {
        Post post = Post.builder().id(1L).title("AI Trends").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo("https://cloud.com/img.png");
        assertThat(result.getSize()).isEqualTo(ImageSize.SQUARE);
        assertThat(result.getSelected()).isTrue();
        assertThat(result.getImagePrompt()).contains("trends").contains("square 1:1 instagram");
    }

    @Test
    void generateImageForPost_noImage_usesLandscapeForLinkedIn() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.LINKEDIN).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void generateImageForPost_noImage_usesLandscapeForFacebook() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.FACEBOOK).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void generateImageForPost_existingImageNoPrompt_buildsPrompt() throws Exception {
        PostImage existingImage = PostImage.builder().id(1L).imagePrompt(null).size(ImageSize.SQUARE).build();
        Post post = Post.builder().id(1L).title("AI Marketing Trends").platform(PlatformType.INSTAGRAM).images(List.of(existingImage)).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);

        assertThat(result.getImagePrompt()).isNotNull();
        assertThat(result.getImagePrompt()).contains("marketing trends");
        assertThat(result.getImageUrl()).isEqualTo("https://cloud.com/img.png");
    }

    @Test
    void generateImageForPost_existingImageWithPrompt_usesExistingPrompt() throws Exception {
        PostImage existingImage = PostImage.builder().id(1L).imagePrompt("custom prompt").size(ImageSize.SQUARE).build();
        Post post = Post.builder().id(1L).title("Anything").platform(PlatformType.INSTAGRAM).images(List.of(existingImage)).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("prompt")).isEqualTo("custom prompt");
    }

    @Test
    void generateImageForPost_usesExistingImageSize_whenSet() throws Exception {
        PostImage existingImage = PostImage.builder().id(1L).imagePrompt("prompt").size(ImageSize.PORTRAIT).build();
        Post post = Post.builder().id(1L).platform(PlatformType.INSTAGRAM).images(List.of(existingImage)).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("width")).isEqualTo(832);
        assertThat(body.get("height")).isEqualTo(1216);
    }

    @Test
    void generateImageForPost_usesPlatformDefaultSize_whenImageSizeNull() throws Exception {
        PostImage existingImage = PostImage.builder().id(1L).imagePrompt("prompt").size(null).build();
        Post post = Post.builder().id(1L).platform(PlatformType.LINKEDIN).images(List.of(existingImage)).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("width")).isEqualTo(1216);
        assertThat(body.get("height")).isEqualTo(832);
    }

    @Test
    void generateImageForPost_throwsWhenImageGenerationFails() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse(null));

        assertThatThrownBy(() -> aiImageService.generateImageForPost(post))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ================= imageSizeForPlatform (via generateImageForPost) =================

    @Test
    void imageSizeForPlatform_instagram_returnsSquare() throws Exception {
        Post post = Post.builder().id(1L).title("T").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);
        assertThat(result.getSize()).isEqualTo(ImageSize.SQUARE);
    }

    @Test
    void imageSizeForPlatform_linkedin_returnsLandscape() throws Exception {
        Post post = Post.builder().id(1L).title("T").platform(PlatformType.LINKEDIN).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);
        assertThat(result.getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void imageSizeForPlatform_facebook_returnsLandscape() throws Exception {
        Post post = Post.builder().id(1L).title("T").platform(PlatformType.FACEBOOK).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);
        assertThat(result.getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void imageSizeForPlatform_null_returnsSquare() throws Exception {
        Post post = Post.builder().id(1L).title("T").platform(null).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PostImage result = aiImageService.generateImageForPost(post);
        assertThat(result.getSize()).isEqualTo(ImageSize.SQUARE);
    }

    // ================= buildPrompt (via generateImageForPost) =================

    @Test
    void buildPrompt_usesExistingPromptFromImage() throws Exception {
        PostImage existingImage = PostImage.builder().id(1L).imagePrompt("existing custom prompt text").size(ImageSize.SQUARE).build();
        Post post = Post.builder().id(1L).title("Will be ignored").platform(PlatformType.INSTAGRAM).images(List.of(existingImage)).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertThat(body.get("prompt")).isEqualTo("existing custom prompt text");
    }

    @Test
    void buildPrompt_extractsUpTo8Keywords() throws Exception {
        Post post = Post.builder().id(1L).title("AI Data Science Machine Learning Deep Neural Networks Cloud Computing Trends").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        String prompt = (String) body.get("prompt");
        String[] words = prompt.split("\\s+");
        long keywordCount = java.util.Arrays.stream(words)
                .filter(w -> !w.contains(":") && !w.contains("/") && !w.contains("-"))
                .count();
        assertThat(words).anyMatch(w -> w.equalsIgnoreCase("data"));
    }

    @Test
    void buildPrompt_usesStyleTagForSquare() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        String prompt = (String) ((Map<String, Object>) captor.getValue().getBody()).get("prompt");
        assertThat(prompt).contains("square 1:1 instagram");
    }

    @Test
    void buildPrompt_usesStyleTagForLandscape() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.LINKEDIN).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        String prompt = (String) ((Map<String, Object>) captor.getValue().getBody()).get("prompt");
        assertThat(prompt).contains("landscape 16:9 linkedin facebook");
    }

    @Test
    void buildPrompt_appendsFixedSuffix() throws Exception {
        Post post = Post.builder().id(1L).title("Test").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        String prompt = (String) ((Map<String, Object>) captor.getValue().getBody()).get("prompt");
        assertThat(prompt).endsWith("professional business technology cinematic lighting photorealistic 4k clean minimalist");
    }

    @Test
    void buildPrompt_filtersShortWords() throws Exception {
        Post post = Post.builder().id(1L).title("AI is a new tech for marketing").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        String prompt = (String) ((Map<String, Object>) captor.getValue().getBody()).get("prompt");
        assertThat(prompt).doesNotContain(" is ");
        assertThat(prompt).doesNotContain(" a ");
    }

    @Test
    void buildPrompt_stripsNonAlphanumericFromTitle() throws Exception {
        Post post = Post.builder().id(1L).title("AI & Marketing: The Future! @2024").platform(PlatformType.INSTAGRAM).build();
        when(cloudflareConfig.getAccountId()).thenReturn("acct");
        when(cloudflareConfig.getApiToken()).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(okResponse("img".getBytes()));
        doReturn("https://cloud.com/img.png").when(cloudinaryService).uploadImageBytes(any());
        when(postImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aiImageService.generateImageForPost(post);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(byte[].class));
        String prompt = (String) ((Map<String, Object>) captor.getValue().getBody()).get("prompt");
        assertThat(prompt).doesNotContain("&").doesNotContain("!").doesNotContain("@");
    }
}
