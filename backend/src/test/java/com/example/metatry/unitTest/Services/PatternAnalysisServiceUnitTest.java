package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatternAnalysisServiceUnitTest {

    @Mock private ContentPatternRepository contentPatternRepository;
    @Mock private ScrapedPostRepository scrapedPostRepository;
    @Mock private GeminiService geminiService;
    @Mock private MemoryContextService memoryContextService;

    @InjectMocks
    private PatternAnalysisService patternAnalysisService;

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
}
