package com.example.metatry.Services;

import com.example.metatry.DTOs.AiContentPostItem;
import com.example.metatry.DTOs.AiContentPostList;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiContentServiceTest {

    @Mock private PromptBuilderService promptBuilderService;
    @Mock private GeminiService geminiService;
    @Mock private MemoryContextService memoryContextService;
    @Mock private PatternAnalysisService patternAnalysisService;
    @Mock private PostRepository postRepository;
    @Mock private PostImageRepository postImageRepository;
    @Mock private ContentPatternRepository contentPatternRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private AiContentService aiContentService;

    @Captor private ArgumentCaptor<List<Post>> postsCaptor;
    @Captor private ArgumentCaptor<List<PostImage>> imagesCaptor;

    @BeforeEach
    void setUp() {
        aiContentService = new AiContentService(
                promptBuilderService, geminiService, memoryContextService,
                patternAnalysisService, postRepository, postImageRepository,
                contentPatternRepository, objectMapper
        );
    }

    @Test
    void generatePostsWithCampaign_withNullInsights_usesDefault() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").platformBreakdown("{\"linkedin\": 1}").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildPlatformPrompt(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"hashtags\": [\"#ai\"], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("T");
        verify(postImageRepository).saveAll(anyList());
    }

    @Test
    void generatePostsWithCampaign_withPatternAndBreakdown_generatesFromPattern() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").platformBreakdown("{\"linkedin\": 2}").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildPlatformPrompt(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T1\", \"content\": \"C1\", \"hashtags\": [\"#ai\"], \"imagePrompt\": \"img1\"}, {\"title\": \"T2\", \"content\": \"C2\", \"hashtags\": [\"#ai\"], \"imagePrompt\": \"img2\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(2);
        verify(postImageRepository).saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).hasSize(2);
        imagesCaptor.getValue().forEach(img -> assertThat(img.getSize()).isEqualTo(ImageSize.LANDSCAPE));
    }

    @Test
    void generatePostsWithCampaign_withoutPattern_usesEstimated() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("AI")).thenReturn(List.of());
        when(promptBuilderService.buildEstimatedPrompt(any(), any(), any(), any())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"platform\": \"linkedin\", \"hashtags\": [\"#ai\"], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatform()).isEqualTo(PlatformType.LINKEDIN);
        verify(postImageRepository).saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).hasSize(1);
        assertThat(imagesCaptor.getValue().get(0).getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void generatePostsWithCampaign_withNullPlatformInItem_skipsPost() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("AI")).thenReturn(List.of());
        when(promptBuilderService.buildEstimatedPrompt(any(), any(), any(), any())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"platform\": \"unknown\", \"hashtags\": [], \"imagePrompt\": \"img\"}]}");

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).isEmpty();
    }

    @Test
    void generatePostsWithCampaign_withUnknownInBreakdown_onlyGeneratesUnknown() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("{\"twitter\": 1}")
                .build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).isEmpty();
    }

    @Test
    void generatePostsWithCampaign_whenGeminiThrows_throwsRuntimeException() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").platformBreakdown("{\"linkedin\": 1}").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildPlatformPrompt(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("Gemini error"));

        assertThrows(RuntimeException.class, () ->
                aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion")
        );
    }

    @Test
    void generatePostsWithCampaign_withInvalidPlatformBreakdown_returnsEmpty() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("invalid json")
                .build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).isEmpty();
        verify(postRepository, never()).saveAll(anyList());
    }

    @Test
    void generatePostsWithCampaign_withNullBreakdown_usesEstimated() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder()
                .topic("AI")
                .platformBreakdown(null)
                .build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildEstimatedPrompt(any(), any(), any(), any())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"platform\": \"linkedin\", \"hashtags\": [], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(1);
        verify(promptBuilderService).buildEstimatedPrompt(any(), any(), any(), any());
    }

    @Test
    void generatePostsWithCampaign_withEmptyBreakdown_usesEstimated() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("")
                .build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildEstimatedPrompt(any(), any(), any(), any())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"platform\": \"instagram\", \"hashtags\": [], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatform()).isEqualTo(PlatformType.INSTAGRAM);
    }

    @Test
    void generatePostsWithCampaign_withEstimatedPosts_whenNoPosts_returnsEmpty() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.empty());
        when(patternAnalysisService.findMatchingPatterns("AI")).thenReturn(List.of());
        when(promptBuilderService.buildEstimatedPrompt(any(), any(), any(), any())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": []}");

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).isEmpty();
        verify(postRepository, never()).saveAll(anyList());
        verify(postImageRepository, never()).saveAll(anyList());
    }

    @Test
    void generatePostsWithCampaign_withFacebookPlatform_createsLandscapeImage() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").platformBreakdown("{\"facebook\": 1}").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildPlatformPrompt(any(), any(), any(), any(), any(), anyInt())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"hashtags\": [], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatform()).isEqualTo(PlatformType.FACEBOOK);
        verify(postImageRepository).saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue().get(0).getSize()).isEqualTo(ImageSize.LANDSCAPE);
    }

    @Test
    void generatePostsWithCampaign_withUnknownPlatformInBreakdown_skips() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        ContentPattern pattern = ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("{\"twitter\": 1, \"linkedin\": 1}")
                .build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));
        when(promptBuilderService.buildPlatformPrompt(any(), any(), any(), any(), any(), anyInt())).thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"posts\": [{\"title\": \"T\", \"content\": \"C\", \"hashtags\": [], \"imagePrompt\": \"img\"}]}");
        when(postRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(postImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Post> result = aiContentService.generatePostsWithCampaign("AI", campaign, "insights", "conclusion");

        assertThat(result).hasSize(1);
    }
}
