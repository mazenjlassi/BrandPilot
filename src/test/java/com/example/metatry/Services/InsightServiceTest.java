package com.example.metatry.Services;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightServiceTest {

    @Mock
    private PostCommentRepository commentRepository;

    @Mock
    private AiInsightService aiInsightService;

    private ObjectMapper objectMapper;
    private InsightService insightService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        insightService = new InsightService(commentRepository, aiInsightService, objectMapper);
    }

    @Test
    void generatePostInsights_whenNoComments_returnsEmpty() {
        when(commentRepository.findByPostId(1L)).thenReturn(List.of());

        PostInsightDTO result = insightService.generatePostInsights(1L);

        assertThat(result.getOverallSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.getSummary()).isEqualTo("No comments available");
    }

    @Test
    void generatePostInsights_withComments_returnsRuleBasedInsight() {
        List<PostComment> comments = List.of(
                PostComment.builder().sentiment("POSITIVE").commentText("Great product!").build(),
                PostComment.builder().sentiment("POSITIVE").commentText("Very useful").build(),
                PostComment.builder().sentiment("NEGATIVE").commentText("Too expensive").build()
        );
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        PostInsightDTO result = insightService.generatePostInsights(1L);

        assertThat(result.getOverallSentiment()).isEqualTo("POSITIVE");
        assertThat(result.getPositiveRatio()).isGreaterThan(0.6);
        assertThat(result.getNegativeRatio()).isLessThan(0.4);
        assertThat(result.getTopPositives()).isNotEmpty();
        assertThat(result.getTopComplaints()).isNotEmpty();
    }

    @Test
    void generatePostInsights_negativeSentiment_generatesNegativeAdvice() {
        List<PostComment> comments = List.of(
                PostComment.builder().sentiment("NEGATIVE").commentText("Terrible experience").build(),
                PostComment.builder().sentiment("NEGATIVE").commentText("Very broken platform").build(),
                PostComment.builder().sentiment("NEGATIVE").commentText("Poor support system").build()
        );
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        PostInsightDTO result = insightService.generatePostInsights(1L);

        assertThat(result.getOverallSentiment()).isEqualTo("NEGATIVE");
        assertThat(result.getAdvice()).contains("improvement");
    }

    @Test
    void generatePostInsights_neutralSentiment_generatesNeutralAdvice() {
        List<PostComment> comments = List.of(
                PostComment.builder().sentiment("NEUTRAL").commentText("Okay product overall").build(),
                PostComment.builder().sentiment("NEUTRAL").commentText("Average quality stuff").build()
        );
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        PostInsightDTO result = insightService.generatePostInsights(1L);

        assertThat(result.getOverallSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.getAdvice()).isNotNull();
        assertThat(result.getIdeas()).contains("Add questions");
    }

    @Test
    void generateCampaignInsights_whenAiSucceeds_mergesResults() throws Exception {
        String aiJson = objectMapper.writeValueAsString(Map.of(
                "overallSentiment", "POSITIVE",
                "topPositives", List.of("quality", "support"),
                "topComplaints", List.of("price"),
                "summary", "Good feedback",
                "advice", "Keep it up",
                "ideas", List.of("Scale campaign")
        ));

        List<PostComment> comments = List.of(
                PostComment.builder().sentiment("POSITIVE").commentText("Excellent!").build(),
                PostComment.builder().sentiment("NEGATIVE").commentText("Bad").build(),
                PostComment.builder().sentiment("NEUTRAL").commentText("Okay").build()
        );
        when(commentRepository.findByPostCampaignId(1L)).thenReturn(comments);
        when(aiInsightService.analyzeComments(any())).thenReturn(aiJson);

        PostInsightDTO result = insightService.generateCampaignInsights(1L);

        assertThat(result.getPositiveRatio()).isEqualTo(1.0 / 3);
        assertThat(result.getNegativeRatio()).isEqualTo(1.0 / 3);
        assertThat(result.getNeutralRatio()).isEqualTo(1.0 / 3);
    }

    @Test
    void generateCampaignInsights_whenAiFails_fallsBackToRuleEngine() {
        List<PostComment> comments = List.of(
                PostComment.builder().sentiment("POSITIVE").commentText("Great!").build()
        );
        when(commentRepository.findByPostCampaignId(1L)).thenReturn(comments);
        when(aiInsightService.analyzeComments(any())).thenThrow(new RuntimeException("AI error"));

        PostInsightDTO result = insightService.generateCampaignInsights(1L);

        assertThat(result.getOverallSentiment()).isEqualTo("POSITIVE");
    }

}
