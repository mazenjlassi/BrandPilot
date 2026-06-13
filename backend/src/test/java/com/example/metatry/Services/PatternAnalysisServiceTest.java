package com.example.metatry.Services;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatternAnalysisServiceTest {

    @Mock private ContentPatternRepository contentPatternRepository;
    @Mock private ScrapedPostRepository scrapedPostRepository;
    @Mock private GeminiService geminiService;
    @Mock private MemoryContextService memoryContextService;

    @InjectMocks
    private PatternAnalysisService patternAnalysisService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(patternAnalysisService, "objectMapper", new ObjectMapper());
    }

    @Test
    void analyzePattern_returnsError_whenNotEnoughPosts() {
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin"))
                .thenReturn(List.of());

        var request = PatternAnalysisRequest.builder()
                .companyName("NVIDIA")
                .platform("linkedin")
                .topic("AI")
                .minPostsRequired(3)
                .build();

        var result = patternAnalysisService.analyzePattern(request);

        assertThat(result.getStatus()).isEqualTo("error");
        assertThat(result.getMessage()).contains("Not enough posts");
    }

    @Test
    void analyzePattern_returnsError_whenLessThanMinPosts() {
        var posts = List.of(
                ScrapedPost.builder().postText("Text 1").build(),
                ScrapedPost.builder().postText("Text 2").build()
        );
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin"))
                .thenReturn(posts);

        var request = PatternAnalysisRequest.builder()
                .companyName("NVIDIA")
                .platform("linkedin")
                .topic("AI")
                .minPostsRequired(3)
                .build();

        var result = patternAnalysisService.analyzePattern(request);

        assertThat(result.getStatus()).isEqualTo("error");
    }

    @Test
    void analyzePattern_sendsPromptToGemini() {
        var posts = List.of(
                ScrapedPost.builder().postText("AI post content").build(),
                ScrapedPost.builder().postText("ML post content").build(),
                ScrapedPost.builder().postText("Deep learning post").build()
        );
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin"))
                .thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{}");
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = PatternAnalysisRequest.builder()
                .companyName("NVIDIA")
                .platform("linkedin")
                .topic("AI")
                .minPostsRequired(3)
                .build();

        patternAnalysisService.analyzePattern(request);

        verify(geminiService).generate(argThat(prompt ->
                prompt.contains("AI") && prompt.contains("AI post content")
        ));
    }

    @Test
    void analyzePattern_parsesAndSavesPattern() {
        var posts = List.of(
                ScrapedPost.builder().postText("Text 1").build(),
                ScrapedPost.builder().postText("Text 2").build(),
                ScrapedPost.builder().postText("Text 3").build()
        );
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin"))
                .thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("""
                {"postFrequency": "3x/week", "contentLength": "150-300 chars", "mediaType": "80% images",
                 "hashtagCount": "3-5", "timingPattern": "Tuesday/Thursday", "tone": "Technical",
                 "ctaStyle": "Links to articles"}""");
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = patternAnalysisService.analyzePattern(
                PatternAnalysisRequest.builder()
                        .companyName("NVIDIA")
                        .platform("linkedin")
                        .topic("AI")
                        .minPostsRequired(3)
                        .build()
        );

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getPostFrequency()).isEqualTo("3x/week");
        assertThat(result.getTone()).isEqualTo("Technical");
    }

    @Test
    void analyzeUnanalyzedBatch_returns0_whenNotEnoughPosts() {
        when(scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse("NVIDIA"))
                .thenReturn(List.of());

        int result = patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA");

        assertThat(result).isEqualTo(0);
    }

    @Test
    void findMatchingPatterns_fallsBackToKeywordSearch() {
        when(contentPatternRepository.findByTopic("Machine Learning")).thenReturn(Optional.empty());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("Machine")).thenReturn(List.of());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("Learning")).thenReturn(List.of(
                ContentPattern.builder().topic("Deep Learning").build()
        ));

        var result = patternAnalysisService.findMatchingPatterns("Machine Learning");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo("Deep Learning");
    }

    @Test
    void getAllPatterns_delegatesToRepository() {
        when(contentPatternRepository.findAll()).thenReturn(List.of(
                ContentPattern.builder().topic("AI").build()
        ));

        var result = patternAnalysisService.getAllPatterns();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo("AI");
    }

    @Test
    void analyzePattern_withoutCompanyName_searchesByPlatform() {
        when(scrapedPostRepository.findByPlatform("linkedin")).thenReturn(List.of());
        var request = PatternAnalysisRequest.builder().topic("AI").platform("linkedin").minPostsRequired(3).build();
        var result = patternAnalysisService.analyzePattern(request);
        assertThat(result.getStatus()).isEqualTo("error");
    }

    @Test
    void analyzePattern_withNullPlatform_defaultsToLinkedin() {
        when(scrapedPostRepository.findByPlatform("linkedin")).thenReturn(List.of());
        var request = PatternAnalysisRequest.builder().topic("AI").minPostsRequired(3).build();
        var result = patternAnalysisService.analyzePattern(request);
        assertThat(result.getStatus()).isEqualTo("error");
    }

    @Test
    void analyzePattern_withNullMinPosts_defaultsTo3() {
        when(scrapedPostRepository.findByPlatform("linkedin")).thenReturn(List.of());
        var request = PatternAnalysisRequest.builder().topic("AI").build();
        var result = patternAnalysisService.analyzePattern(request);
        assertThat(result.getStatus()).isEqualTo("error");
    }

    @Test
    void analyzePattern_whenGeminiThrowsException_returnsError() {
        var posts = List.of(
                ScrapedPost.builder().postText("Text 1").build(),
                ScrapedPost.builder().postText("Text 2").build(),
                ScrapedPost.builder().postText("Text 3").build()
        );
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("API error"));

        var result = patternAnalysisService.analyzePattern(
                PatternAnalysisRequest.builder().companyName("NVIDIA").platform("linkedin").topic("AI").minPostsRequired(3).build()
        );

        assertThat(result.getStatus()).isEqualTo("error");
        assertThat(result.getMessage()).contains("API error");
    }

    @Test
    void getPatternsByCompany_delegatesToRepository() {
        when(contentPatternRepository.findByCompanyName("NVIDIA")).thenReturn(List.of(
                ContentPattern.builder().topic("AI").build()
        ));
        var result = patternAnalysisService.getPatternsByCompany("NVIDIA");
        assertThat(result).hasSize(1);
    }

    @Test
    void getPatternByTopic_returnsPattern() {
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(
                ContentPattern.builder().topic("AI").build()
        ));
        var result = patternAnalysisService.getPatternByTopic("AI");
        assertThat(result).isNotNull();
        assertThat(result.getTopic()).isEqualTo("AI");
    }

    @Test
    void getPatternByTopic_whenNotFound_returnsNull() {
        when(contentPatternRepository.findByTopic("Unknown")).thenReturn(Optional.empty());
        var result = patternAnalysisService.getPatternByTopic("Unknown");
        assertThat(result).isNull();
    }

    @Test
    void findMatchingPatterns_withExactMatch_returnsDirect() {
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(
                ContentPattern.builder().topic("AI").build()
        ));
        var result = patternAnalysisService.findMatchingPatterns("AI");
        assertThat(result).hasSize(1);
        verify(contentPatternRepository, never()).findByTopicContainingIgnoreCase(anyString());
    }

    @Test
    void findMatchingPatterns_keepsLongerKeywords_andSkipsShort() {
        when(contentPatternRepository.findByTopic("AI learning")).thenReturn(Optional.empty());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("learning")).thenReturn(List.of(
                ContentPattern.builder().topic("Deep learning").build()
        ));
        var result = patternAnalysisService.findMatchingPatterns("AI learning");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo("Deep learning");
    }

    @Test
    void analyzeUnanalyzedBatch_withEnoughPosts_processesCampaign() {
        var posts = List.of(
                ScrapedPost.builder().id(1L).postText("Post 1").platform("linkedin").build(),
                ScrapedPost.builder().id(2L).postText("Post 2").platform("instagram").build(),
                ScrapedPost.builder().id(3L).postText("Post 3").platform("facebook").build()
        );
        when(scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse("NVIDIA")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("""
                [{"campaignName": "Test Campaign", "topic": "AI", "postIndices": [0, 1, 2],
                  "platformBreakdown": {"linkedin": 1, "instagram": 1, "facebook": 1},
                  "tone": "Educational", "contentLength": "200", "mediaType": "image",
                  "hashtagCount": "3", "timingPattern": "morning", "ctaStyle": "question"}]
                """);
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        int result = patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA");

        assertThat(result).isEqualTo(1);
        verify(scrapedPostRepository).saveAll(posts);
        assertThat(posts.get(0).getUsedForPattern()).isTrue();
    }

    @Test
    void analyzeUnanalyzedBatch_whenResponseIsObject_parsesCampaigns() {
        var posts = List.of(
                ScrapedPost.builder().id(1L).postText("Post 1").platform("linkedin").build(),
                ScrapedPost.builder().id(2L).postText("Post 2").platform("instagram").build(),
                ScrapedPost.builder().id(3L).postText("Post 3").platform("facebook").build()
        );
        when(scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse("NVIDIA")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("""
                {"campaigns": [{"campaignName": "Campaign 1", "topic": "AI", "postIndices": [0, 1],
                  "platformBreakdown": {"linkedin": 1}, "tone": "Edu", "contentLength": "200",
                  "mediaType": "img", "hashtagCount": "3", "timingPattern": "am", "ctaStyle": "q"}],
                  "campaignIndices": [2]}
                """);
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        int result = patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA");

        assertThat(result).isEqualTo(1);
    }

    @Test
    void analyzeUnanalyzedBatch_whenNoValidIndices_returns0() {
        var posts = List.of(
                ScrapedPost.builder().id(1L).postText("Post 1").platform("linkedin").build(),
                ScrapedPost.builder().id(2L).postText("Post 2").platform("instagram").build(),
                ScrapedPost.builder().id(3L).postText("Post 3").platform("facebook").build()
        );
        when(scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse("NVIDIA")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("""
                [{"campaignName": "C1", "topic": "AI", "postIndices": [5]}]
                """);

        int result = patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA");

        assertThat(result).isEqualTo(0);
    }

    @Test
    void analyzeUnanalyzedBatch_whenJsonParseFails_returns0() {
        var posts = List.of(
                ScrapedPost.builder().id(1L).postText("Post 1").platform("linkedin").build(),
                ScrapedPost.builder().id(2L).postText("Post 2").platform("instagram").build(),
                ScrapedPost.builder().id(3L).postText("Post 3").platform("facebook").build()
        );
        when(scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse("NVIDIA")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("invalid json");

        int result = patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA");

        assertThat(result).isEqualTo(0);
    }

    @Test
    void analyzePattern_parsesJsonWithRegex_extractsFields() {
        var posts = List.of(
                ScrapedPost.builder().postText("Text 1").build(),
                ScrapedPost.builder().postText("Text 2").build(),
                ScrapedPost.builder().postText("Text 3").build()
        );
        when(scrapedPostRepository.findByCompanyNameAndPlatform("NVIDIA", "linkedin")).thenReturn(posts);
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("""
                {"postFrequency": "daily", "contentLength": "short", "mediaType": "text",
                 "hashtagCount": "none", "timingPattern": "evenings", "tone": "Casual",
                 "ctaStyle": "Ask questions"}
                """);
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = patternAnalysisService.analyzePattern(
                PatternAnalysisRequest.builder().companyName("NVIDIA").platform("linkedin").topic("AI").minPostsRequired(3).build()
        );

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getPostFrequency()).isEqualTo("daily");
        assertThat(result.getContentLength()).isEqualTo("short");
        assertThat(result.getMediaType()).isEqualTo("text");
        assertThat(result.getHashtagCount()).isEqualTo("none");
        assertThat(result.getTimingPattern()).isEqualTo("evenings");
        assertThat(result.getTone()).isEqualTo("Casual");
        assertThat(result.getCtaStyle()).isEqualTo("Ask questions");
    }
}
