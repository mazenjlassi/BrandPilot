package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.AiContentPostItem;
import com.example.metatry.DTOs.AiContentPostList;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiContentServiceUnitTest {

    @Mock
    private PromptBuilderService promptBuilderService;
    @Mock
    private GeminiService geminiService;
    @Mock
    private MemoryContextService memoryContextService;
    @Mock
    private PatternAnalysisService patternAnalysisService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostImageRepository postImageRepository;
    @Mock
    private ContentPatternRepository contentPatternRepository;
    @Mock
    private ObjectMapper objectMapper;

    private AiContentService aiContentService;

    @Captor
    private ArgumentCaptor<List<Post>> postListCaptor;
    @Captor
    private ArgumentCaptor<List<PostImage>> imageListCaptor;

    private Campaign campaign;
    private ContentPattern patternWithBreakdown;
    private AiContentPostItem fbItem;
    private AiContentPostList postList;
    private static final String AI_TEXT = "{\"posts\":[]}";

    @BeforeEach
    void setUp() throws Exception {
        aiContentService = new AiContentService(
                promptBuilderService, geminiService, memoryContextService,
                patternAnalysisService, postRepository, postImageRepository,
                contentPatternRepository, objectMapper
        );

        campaign = Campaign.builder().id(1L).topic("tech").build();

        patternWithBreakdown = ContentPattern.builder()
                .platformBreakdown("{\"facebook\":2,\"linkedin\":1}")
                .build();

        fbItem = new AiContentPostItem();
        fbItem.setPlatform("facebook");
        fbItem.setTitle("FB Post");
        fbItem.setContent("FB content");
        fbItem.setHashtags(List.of("#tech"));
        fbItem.setImagePrompt("A tech image");

        postList = new AiContentPostList();
        postList.setPosts(List.of(fbItem));

        lenient().when(objectMapper.readValue(eq(AI_TEXT), eq(AiContentPostList.class)))
                .thenReturn(postList);
    }

    // ============ generatePostsWithCampaign ============

    @Test
    void generatePostsWithCampaign_nullsInsightsAndConclusion_usesDefaults() throws Exception {
        when(contentPatternRepository.findByTopic("tech")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("tech")).thenReturn(List.of());
        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(1L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = aiContentService.generatePostsWithCampaign("tech", campaign, null, null);

        assertThat(result).hasSize(1);
        verify(promptBuilderService).buildEstimatedPrompt(
                eq("tech"), eq("No insights available"),
                eq("Focus on engagement, clarity, and value"), isNull());
    }

    @Test
    void generatePostsWithCampaign_withExactPatternBreakdown_generatesPlatformPosts() throws Exception {
        when(contentPatternRepository.findByTopic("tech")).thenReturn(Optional.of(patternWithBreakdown));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of("facebook", 2, "linkedin", 1));
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), any(), anyInt())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(1L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = aiContentService.generatePostsWithCampaign("tech", campaign, "Good insights", "Great conclusion");

        assertThat(result).hasSize(2);
    }

    @Test
    void generatePostsWithCampaign_withPatternNullBreakdown_fallsBackToEstimated() throws Exception {
        ContentPattern noBreakdown = ContentPattern.builder().platformBreakdown(null).build();
        when(contentPatternRepository.findByTopic("tech")).thenReturn(Optional.of(noBreakdown));
        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("estimated");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("estimated\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(1L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = aiContentService.generatePostsWithCampaign("tech", campaign, null, null);

        assertThat(result).hasSize(1);
        verify(promptBuilderService).buildEstimatedPrompt(anyString(), anyString(), anyString(), eq(noBreakdown));
    }

    @Test
    void generatePostsWithCampaign_noPattern_fallsBackToEstimatedWithNull() throws Exception {
        when(contentPatternRepository.findByTopic("tech")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("tech")).thenReturn(List.of());
        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), isNull()))
                .thenReturn("estimated");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("estimated\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(1L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = aiContentService.generatePostsWithCampaign("tech", campaign, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void generatePostsWithCampaign_whenPatternAnalysisFindsMatch_usesPattern() throws Exception {
        ContentPattern matchedPattern = ContentPattern.builder()
                .platformBreakdown("{\"facebook\":1}")
                .build();
        when(contentPatternRepository.findByTopic("tech")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("tech")).thenReturn(List.of(matchedPattern));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of("facebook", 1));
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), any(), anyInt())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(5L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = aiContentService.generatePostsWithCampaign("tech", campaign, null, null);

        assertThat(result).hasSize(1);
    }

    // ============ generateFromPattern (via ReflectionTestUtils) ============

    @Test
    void generateFromPattern_returnsEmptyWhenBreakdownParsingFails() throws Exception {
        ContentPattern badPattern = ContentPattern.builder().platformBreakdown("invalid json").build();
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(null);

        List<Post> result = invokeGenerateFromPattern(badPattern);

        assertThat(result).isEmpty();
        verifyNoInteractions(geminiService);
    }

    @Test
    void generateFromPattern_skipsUnknownPlatformKeys() throws Exception {
        ContentPattern mixedPattern = ContentPattern.builder()
                .platformBreakdown("{\"facebook\":1,\"tiktok\":2}").build();

        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of("facebook", 1, "tiktok", 2));
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), eq(PlatformType.FACEBOOK), eq(1))).thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(postRepository.saveAll(any())).thenReturn(List.of(
                Post.builder().id(1L).platform(PlatformType.FACEBOOK).build()
        ));

        List<Post> result = invokeGenerateFromPattern(mixedPattern);

        assertThat(result).hasSize(1);
    }

    // ============ generatePlatformPosts (via ReflectionTestUtils) ============

    @Test
    void generatePlatformPosts_createsPostsAndImages() throws Exception {
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), any(), anyInt())).thenReturn("platform prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("platform prompt\n\nmemory")).thenReturn(AI_TEXT);

        Post savedPost = Post.builder().id(10L).platform(PlatformType.FACEBOOK).build();
        when(postRepository.saveAll(any())).thenReturn(List.of(savedPost));

        List<Post> result = invokeGeneratePlatformPosts(PlatformType.FACEBOOK, 1);

        assertThat(result).hasSize(1);
        verify(postImageRepository).saveAll(imageListCaptor.capture());
        List<PostImage> images = imageListCaptor.getValue();
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getSize()).isEqualTo(ImageSize.LANDSCAPE);
        assertThat(images.get(0).getImagePrompt()).isEqualTo("A tech image");
    }

    @Test
    void generatePlatformPosts_instagramUsesSquareImageSize() throws Exception {
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), any(), anyInt())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);

        Post savedPost = Post.builder().id(11L).platform(PlatformType.INSTAGRAM).build();
        when(postRepository.saveAll(any())).thenReturn(List.of(savedPost));

        List<Post> result = invokeGeneratePlatformPosts(PlatformType.INSTAGRAM, 1);

        verify(postImageRepository).saveAll(imageListCaptor.capture());
        assertThat(imageListCaptor.getValue().get(0).getSize()).isEqualTo(ImageSize.SQUARE);
    }

    @Test
    void generatePlatformPosts_throwsRuntimeExceptionOnError() {
        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(),
                any(), any(), anyInt())).thenThrow(new RuntimeException("Prompt failed"));

        assertThatThrownBy(() -> invokeGeneratePlatformPosts(PlatformType.FACEBOOK, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error generating FACEBOOK posts");
    }

    // ============ generateEstimatedPosts (via ReflectionTestUtils) ============

    @Test
    void generateEstimatedPosts_createsPostsAndImages() throws Exception {
        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("estimated prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("estimated prompt\n\nmemory")).thenReturn(AI_TEXT);

        Post savedPost = Post.builder().id(12L).platform(PlatformType.FACEBOOK).build();
        when(postRepository.saveAll(any())).thenReturn(List.of(savedPost));

        List<Post> result = invokeGenerateEstimatedPosts();

        assertThat(result).hasSize(1);
        verify(postImageRepository).saveAll(any());
    }

    @Test
    void generateEstimatedPosts_emptyPostList_returnsEmpty() throws Exception {
        AiContentPostList emptyList = new AiContentPostList();
        emptyList.setPosts(List.of());

        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(objectMapper.readValue(eq(AI_TEXT), eq(AiContentPostList.class))).thenReturn(emptyList);

        List<Post> result = invokeGenerateEstimatedPosts();

        assertThat(result).isEmpty();
        verifyNoInteractions(postRepository);
    }

    @Test
    void generateEstimatedPosts_skipsUnknownPlatform() throws Exception {
        AiContentPostItem unknownItem = new AiContentPostItem();
        unknownItem.setPlatform("tiktok");
        unknownItem.setTitle("TikTok");
        AiContentPostList list = new AiContentPostList();
        list.setPosts(List.of(fbItem, unknownItem));

        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext("tech")).thenReturn("memory");
        when(geminiService.generate("prompt\n\nmemory")).thenReturn(AI_TEXT);
        when(objectMapper.readValue(eq(AI_TEXT), eq(AiContentPostList.class))).thenReturn(list);

        Post savedPost = Post.builder().id(13L).platform(PlatformType.FACEBOOK).build();
        when(postRepository.saveAll(any())).thenReturn(List.of(savedPost));

        List<Post> result = invokeGenerateEstimatedPosts();

        assertThat(result).hasSize(1);
    }

    @Test
    void generateEstimatedPosts_throwsRuntimeExceptionOnError() {
        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Estimated failed"));

        assertThatThrownBy(() -> invokeGenerateEstimatedPosts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error generating AI posts");
    }

    // ============ createPost ============

    @Test
    void createPost_setsDefaultFields() {
        Post result = invokeCreatePost("Test Title", "Test content", List.of("#tag"),
                PlatformType.FACEBOOK, campaign);

        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getHashtags()).isEqualTo("#tag");
        assertThat(result.getPlatform()).isEqualTo(PlatformType.FACEBOOK);
        assertThat(result.getGeneratedByAI()).isTrue();
        assertThat(result.getApproved()).isFalse();
        assertThat(result.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(result.getCampaign()).isEqualTo(campaign);
        assertThat(result.getLink()).isNotNull();
    }

    @Test
    void createPost_handlesNullHashtags() {
        Post result = invokeCreatePost("Title", "Content", null, PlatformType.LINKEDIN, campaign);
        assertThat(result.getHashtags()).isEmpty();
    }

    // ============ mapToPlatformType ============

    @Test
    void mapToPlatformType_returnsNullForNullInput() {
        assertThat(invokeMapToPlatformType(null)).isNull();
    }

    @Test
    void mapToPlatformType_returnsNullForUnknownPlatform() {
        assertThat(invokeMapToPlatformType("tiktok")).isNull();
    }

    @Test
    void mapToPlatformType_returnsPlatformForKnownTypes() {
        assertThat(invokeMapToPlatformType("linkedin")).isEqualTo(PlatformType.LINKEDIN);
        assertThat(invokeMapToPlatformType("instagram")).isEqualTo(PlatformType.INSTAGRAM);
        assertThat(invokeMapToPlatformType("facebook")).isEqualTo(PlatformType.FACEBOOK);
        assertThat(invokeMapToPlatformType("LINKEDIN")).isEqualTo(PlatformType.LINKEDIN);
    }

    // ============ Reflection helpers ============

    @SuppressWarnings("unchecked")
    private List<Post> invokeGenerateFromPattern(ContentPattern pattern) {
        return (List<Post>) ReflectionTestUtils.invokeMethod(aiContentService, "generateFromPattern",
                "tech", campaign, "insights", "conclusion", pattern);
    }

    @SuppressWarnings("unchecked")
    private List<Post> invokeGeneratePlatformPosts(PlatformType platform, int count) {
        return (List<Post>) ReflectionTestUtils.invokeMethod(aiContentService, "generatePlatformPosts",
                "tech", campaign, "insights", "conclusion",
                patternWithBreakdown, platform, count);
    }

    @SuppressWarnings("unchecked")
    private List<Post> invokeGenerateEstimatedPosts() {
        return (List<Post>) ReflectionTestUtils.invokeMethod(aiContentService, "generateEstimatedPosts",
                "tech", campaign, "insights", "conclusion", null);
    }

    private Object invokeMapToPlatformType(String key) {
        return ReflectionTestUtils.invokeMethod(aiContentService, "mapToPlatformType", key);
    }

    private Post invokeCreatePost(String title, String content, List<String> hashtags,
                                   PlatformType platform, Campaign campaign) {
        return (Post) ReflectionTestUtils.invokeMethod(aiContentService, "createPost",
                title, content, hashtags, platform, campaign);
    }
}
