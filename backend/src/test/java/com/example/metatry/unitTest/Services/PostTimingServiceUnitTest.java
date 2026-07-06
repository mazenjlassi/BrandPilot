package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.TimingAnalysisDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostTimingServiceUnitTest {

    @Mock
    private PostCommentRepository commentRepository;

    private PostTimingService postTimingService;

    @BeforeEach
    void setUp() {
        postTimingService = new PostTimingService(commentRepository);
    }

    @Test
    void analyzeBestPostingTimes_whenNoComments_returnsDefault() {
        when(commentRepository.findByPlatformAndCreatedAtAfter(
                any(), any()))
                .thenReturn(List.of(), List.of());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookTotalComments()).isZero();
        assertThat(result.getInstagramTotalComments()).isZero();
        assertThat(result.getFacebookBestHour()).isEqualTo("12:00 PM");
        assertThat(result.getInstagramBestHour()).isEqualTo("12:00 PM");
        assertThat(result.getRecommendation())
                .contains("post on Facebook at 12:00 PM and Instagram at 12:00 PM");
    }

    @Test
    void analyzeBestPostingTimes_withComments_calculatesBestHours() {
        PostComment fbComment1 = PostComment.builder()
                .createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
                .build();
        PostComment fbComment2 = PostComment.builder()
                .createdAt(LocalDateTime.of(2024, 1, 1, 9, 30))
                .build();
        PostComment fbComment3 = PostComment.builder()
                .createdAt(LocalDateTime.of(2024, 1, 1, 14, 0))
                .build();
        PostComment igComment1 = PostComment.builder()                
                .createdAt(LocalDateTime.of(2024, 1, 1, 20, 0))
                .build();

        when(commentRepository.findByPlatformAndCreatedAtAfter(
                any(), any()))
                .thenReturn(List.of(fbComment1, fbComment2, fbComment3))
                .thenReturn(List.of(igComment1));

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookTotalComments()).isEqualTo(3);
        assertThat(result.getInstagramTotalComments()).isEqualTo(1);
        assertThat(result.getFacebookBestHour()).isEqualTo("9:00 AM");
        assertThat(result.getInstagramBestHour()).isEqualTo("8:00 PM");
    }

    @Test
    void analyzeBestPostingTimes_returnsDistributions() {
        PostComment fbComment = PostComment.builder()
                .createdAt(LocalDateTime.of(2024, 6, 15, 10, 0))
                .build();

        when(commentRepository.findByPlatformAndCreatedAtAfter(
                any(), any()))
                .thenReturn(List.of(fbComment))
                .thenReturn(List.of());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getHourlyDistribution()).isNotEmpty();
        assertThat(result.getDailyDistribution()).isNotEmpty();
    }

    @Test
    void analyzeBestPostingTimes_ignoresNullCreatedAt() {
        PostComment fbComment = PostComment.builder()
                .createdAt(null)
                .build();

        when(commentRepository.findByPlatformAndCreatedAtAfter(
                any(), any()))
                .thenReturn(List.of(fbComment))
                .thenReturn(List.of());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookBestHour()).isEqualTo("12:00 PM");
    }
}
