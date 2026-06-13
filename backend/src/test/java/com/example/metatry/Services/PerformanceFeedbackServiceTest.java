package com.example.metatry.Services;

import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceFeedbackServiceTest {

    @Mock private ContentPatternRepository contentPatternRepository;
    @Mock private PostRepository postRepository;
    @Mock private GeminiService geminiService;

    @InjectMocks
    private PerformanceFeedbackService performanceFeedbackService;

    @Test
    void updatePatternsFromPerformance_whenNoNewPosts_returnsEarly() {
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of());

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository, never()).findAll();
    }

    @Test
    void updatePatternsFromPerformance_whenNoPatterns_updatesLastFeedbackRun() {
        Post post = Post.builder().engagementScore(0.5).publishedAt(LocalDateTime.now()).content("c").build();
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(post));
        when(contentPatternRepository.findAll()).thenReturn(List.of());

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository, never()).save(any());
    }

    @Test
    void updatePatternsFromPerformance_withPostsAndPatterns_updatesPatterns() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        Post post = Post.builder()
                .engagementScore(0.8)
                .publishedAt(LocalDateTime.now())
                .content("Some content for best post display")
                .campaign(campaign)
                .build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").build();

        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(post));
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(anyString())).thenReturn("Great performance advice");
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(pattern);
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.8);
        assertThat(pattern.getTotalPostsGenerated()).isEqualTo(1);
        assertThat(pattern.getPerformanceAdvice()).isEqualTo("Great performance advice");
    }

    @Test
    void updatePatternsFromPerformance_withNullTopicPattern_usesZeroEngagement() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        Post post = Post.builder()
                .engagementScore(0.5)
                .publishedAt(LocalDateTime.now())
                .content("Some content")
                .campaign(campaign)
                .build();
        ContentPattern pattern = ContentPattern.builder().contentLength("short").build();

        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(post));
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(anyString())).thenReturn("advice");
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(pattern);
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.0);
    }

    @Test
    void updatePatternsFromPerformance_withMatchingTopic_updatesEngagement() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        Post post = Post.builder()
                .engagementScore(0.75)
                .publishedAt(LocalDateTime.now())
                .content("Some content")
                .campaign(campaign)
                .build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").build();

        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(post));
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(anyString())).thenReturn("advice");
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        performanceFeedbackService.updatePatternsFromPerformance();

        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.75);
    }

    @Test
    void updatePatternsFromPerformance_whenGeminiFails_savesWithoutAdvice() {
        Campaign campaign = Campaign.builder().topic("AI").build();
        Post post = Post.builder()
                .engagementScore(0.5)
                .publishedAt(LocalDateTime.now())
                .content("Some content")
                .campaign(campaign)
                .build();
        ContentPattern pattern = ContentPattern.builder().topic("AI").build();

        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(post));
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("Gemini error"));
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(pattern);
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.5);
    }

    @Test
    void getPatternsByPerformance_filtersAndSortsByEngagement() {
        ContentPattern high = ContentPattern.builder().topic("High").avgEngagementScore(0.9).build();
        ContentPattern low = ContentPattern.builder().topic("Low").avgEngagementScore(0.3).build();
        ContentPattern none = ContentPattern.builder().topic("None").build();

        when(contentPatternRepository.findAll()).thenReturn(List.of(low, high, none));

        List<ContentPattern> result = performanceFeedbackService.getPatternsByPerformance();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTopic()).isEqualTo("High");
        assertThat(result.get(1).getTopic()).isEqualTo("Low");
    }
}
