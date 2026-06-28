package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiInsightService;
import com.example.metatry.Services.InsightService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class InsightServiceIntegrationTest {

    @MockitoBean
    private AiInsightService aiInsightService;

    @Autowired
    private InsightService insightService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private Post savedPost;
    private Long campaignId;

    @BeforeEach
    void setUp() {
        when(aiInsightService.analyzeComments(anyList()))
                .thenReturn("{\"overallSentiment\":\"POSITIVE\",\"topPositives\":[\"Great content\",\"clean UI\"],\"topComplaints\":[],\"summary\":\"Good engagement\",\"advice\":\"Keep posting\",\"ideas\":[\"More video\"]}");

        Campaign c = campaignRepository.save(
                Campaign.builder().name("InsightSvcCamp").topic("AI").build());
        campaignId = c.getId();
        savedPost = postRepository.save(Post.builder()
                .title("Insight Svc Post")
                .content("Test content")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED)
                .campaign(c)
                .likes(10)
                .commentsCount(3)
                .impressions(100)
                .build());
    }

    @AfterEach
    void tearDown() {
        postCommentRepository.findByPostId(savedPost.getId())
                .forEach(pc -> postCommentRepository.delete(pc));
        postRepository.findById(savedPost.getId())
                .ifPresent(p -> postRepository.delete(p));
        if (campaignId != null) {
            campaignRepository.deleteById(campaignId);
        }
    }

    // ================= generatePostInsights =================

    @Test
    void generatePostInsights_noComments_returnsEmptyInsight() {
        PostInsightDTO result = insightService.generatePostInsights(savedPost.getId());

        assertThat(result.getOverallSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.getPositiveRatio()).isZero();
        assertThat(result.getNegativeRatio()).isZero();
        assertThat(result.getNeutralRatio()).isEqualTo(1.0);
        assertThat(result.getTopComplaints()).isEmpty();
        assertThat(result.getSummary()).isEqualTo("No comments available");
        assertThat(result.getAdvice()).isEqualTo("No data available yet");
    }

    @Test
    void generatePostInsights_mixedComments_returnsPositiveSentiment() {
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Excellent work!").sentiment("POSITIVE").authorName("U1").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Good job").sentiment("POSITIVE").authorName("U2").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Could be better").sentiment("NEUTRAL").authorName("U3").build());

        PostInsightDTO result = insightService.generatePostInsights(savedPost.getId());

        assertThat(result.getOverallSentiment()).isEqualTo("POSITIVE");
        assertThat(result.getPositiveRatio()).isEqualTo(2.0 / 3);
        assertThat(result.getNeutralRatio()).isEqualTo(1.0 / 3);
        assertThat(result.getNegativeRatio()).isZero();
        assertThat(result.getTopPositives()).isNotEmpty();
        assertThat(result.getSummary()).contains("POSITIVE");
    }

    @Test
    void generatePostInsights_allNegative_returnsNegativeSentiment() {
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Terrible product").sentiment("NEGATIVE").authorName("U1").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Very disappointed").sentiment("NEGATIVE").authorName("U2").build());

        PostInsightDTO result = insightService.generatePostInsights(savedPost.getId());

        assertThat(result.getOverallSentiment()).isEqualTo("NEGATIVE");
        assertThat(result.getNegativeRatio()).isEqualTo(1.0);
        assertThat(result.getAdvice()).contains("improvement");
    }

    @Test
    void generatePostInsights_allNeutral_returnsNeutralSentiment() {
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Okay nothing special").sentiment("NEUTRAL").authorName("U1").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("It is fine I guess").sentiment("NEUTRAL").authorName("U2").build());

        PostInsightDTO result = insightService.generatePostInsights(savedPost.getId());

        assertThat(result.getOverallSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.getNeutralRatio()).isEqualTo(1.0);
    }

    @Test
    void generatePostInsights_negativeMajorityWithSomePositive_returnsNegativeSentiment() {
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Worst experience ever").sentiment("NEGATIVE").authorName("U1").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Not recommended").sentiment("NEGATIVE").authorName("U2").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Decent product").sentiment("NEUTRAL").authorName("U3").build());

        PostInsightDTO result = insightService.generatePostInsights(savedPost.getId());

        assertThat(result.getOverallSentiment()).isEqualTo("NEGATIVE");
        assertThat(result.getNegativeRatio()).isEqualTo(2.0 / 3);
    }

    // ================= generateCampaignInsights =================

    @Test
    void generateCampaignInsights_noComments_returnsNeutralFallbackWithMetrics() {
        PostInsightDTO result = insightService.generateCampaignInsights(campaignId);

        assertThat(result.getOverallSentiment()).isEqualTo("NEUTRAL");
        assertThat(result.getPositiveRatio()).isZero();
        assertThat(result.getNegativeRatio()).isZero();
        assertThat(result.getNeutralRatio()).isEqualTo(1.0);
        assertThat(result.getSummary()).isEqualTo("No comments available");
        assertThat(result.getTotalLikes()).isEqualTo(10);
        assertThat(result.getTotalComments()).isEqualTo(3);
        assertThat(result.getReach()).isEqualTo(100);
    }

    @Test
    void generateCampaignInsights_withCommentsAndAiSuccess_mergesAiAndDbData() {
        when(aiInsightService.analyzeComments(anyList()))
                .thenReturn("{\"overallSentiment\":\"POSITIVE\",\"topPositives\":[\"Great content\"],\"topComplaints\":[],\"summary\":\"AI summary\",\"advice\":\"AI advice\",\"ideas\":[\"AI idea\"]}");

        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Love this!").sentiment("POSITIVE").authorName("U1").build());
        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Hate this").sentiment("NEGATIVE").authorName("U2").build());

        PostInsightDTO result = insightService.generateCampaignInsights(campaignId);

        assertThat(result.getOverallSentiment()).isEqualTo("POSITIVE");
        assertThat(result.getPositiveRatio()).isEqualTo(0.5);
        assertThat(result.getNegativeRatio()).isEqualTo(0.5);
        assertThat(result.getSummary()).isEqualTo("AI summary");
        assertThat(result.getAdvice()).isEqualTo("AI advice");
        assertThat(result.getIdeas()).contains("AI idea");
        assertThat(result.getTotalLikes()).isEqualTo(10);
        assertThat(result.getTotalComments()).isEqualTo(3);
        assertThat(result.getReach()).isEqualTo(100);
    }

    @Test
    void generateCampaignInsights_withCommentsAndAiFailure_fallsBackToRuleEngine() {
        when(aiInsightService.analyzeComments(anyList()))
                .thenThrow(new RuntimeException("Gemini API error"));

        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Excellent work!").sentiment("POSITIVE").authorName("U1").build());

        PostInsightDTO result = insightService.generateCampaignInsights(campaignId);

        assertThat(result.getOverallSentiment()).isEqualTo("POSITIVE");
        assertThat(result.getPositiveRatio()).isEqualTo(1.0);
        assertThat(result.getNegativeRatio()).isZero();
        assertThat(result.getTotalLikes()).isEqualTo(10);
        assertThat(result.getReach()).isEqualTo(100);
    }

    @Test
    void generateCampaignInsights_withCommentsAndInvalidJson_usesFallback() {
        when(aiInsightService.analyzeComments(anyList()))
                .thenReturn("not valid json at all");

        postCommentRepository.save(PostComment.builder()
                .post(savedPost).commentText("Nice post").sentiment("POSITIVE").authorName("U1").build());

        PostInsightDTO result = insightService.generateCampaignInsights(campaignId);

        assertThat(result.getPositiveRatio()).isEqualTo(1.0);
        assertThat(result.getTotalLikes()).isEqualTo(10);
        assertThat(result.getReach()).isEqualTo(100);
    }
}
