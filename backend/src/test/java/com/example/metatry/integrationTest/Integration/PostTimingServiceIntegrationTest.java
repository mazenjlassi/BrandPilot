package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.TimingAnalysisDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.PostTimingService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PostTimingServiceIntegrationTest {

    @Autowired
    private PostTimingService postTimingService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private Campaign campaign;
    private Post post;

    @BeforeEach
    void setUp() {
        campaign = campaignRepository.save(
                Campaign.builder().name("TimingCamp").topic("test").build());
        post = postRepository.save(Post.builder()
                .title("Timing Post").content("Content")
                .platform(PlatformType.FACEBOOK).status(PostStatus.PUBLISHED)
                .campaign(campaign).build());
    }

    @AfterEach
    void tearDown() {
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @Test
    void analyzeBestPostingTimes_withComments_returnsAnalysis() {
        postCommentRepository.save(PostComment.builder()
                .commentText("Great post").authorName("User1")
                .createdAt(LocalDateTime.now().withHour(10).minusDays(1))
                .post(post).build());
        postCommentRepository.save(PostComment.builder()
                .commentText("Love this").authorName("User2")
                .createdAt(LocalDateTime.now().withHour(10).minusDays(2))
                .post(post).build());
        postCommentRepository.save(PostComment.builder()
                .commentText("Nice").authorName("User3")
                .createdAt(LocalDateTime.now().withHour(14).minusDays(3))
                .post(post).build());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result).isNotNull();
        assertThat(result.getFacebookTotalComments()).isEqualTo(3);
        assertThat(result.getInstagramTotalComments()).isZero();
        assertThat(result.getFacebookBestHour()).isEqualTo("10:00 AM");
        assertThat(result.getHourlyDistribution()).isNotEmpty();
        assertThat(result.getDailyDistribution()).isNotEmpty();
        assertThat(result.getRecommendation()).contains("Facebook").contains("Instagram");
    }

    @Test
    void analyzeBestPostingTimes_noComments_returnsDefaultHours() {
        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookTotalComments()).isZero();
        assertThat(result.getInstagramTotalComments()).isZero();
        assertThat(result.getFacebookBestHour()).isEqualTo("12:00 PM");
        assertThat(result.getInstagramBestHour()).isEqualTo("12:00 PM");
        assertThat(result.getHourlyDistribution()).isEmpty();
    }

    @Test
    void analyzeBestPostingTimes_withInstagramComments_includesBoth() {
        Post igPost = postRepository.save(Post.builder()
                .title("IG Post").content("IG")
                .platform(PlatformType.INSTAGRAM).status(PostStatus.PUBLISHED)
                .campaign(campaign).build());

        postCommentRepository.save(PostComment.builder()
                .commentText("FB comment").authorName("U1")
                .createdAt(LocalDateTime.now().withHour(9).minusDays(1))
                .post(post).build());
        postCommentRepository.save(PostComment.builder()
                .commentText("IG comment").authorName("U2")
                .createdAt(LocalDateTime.now().withHour(20).minusDays(1))
                .post(igPost).build());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookTotalComments()).isEqualTo(1);
        assertThat(result.getInstagramTotalComments()).isEqualTo(1);
        assertThat(result.getFacebookBestHour()).isEqualTo("9:00 AM");
        assertThat(result.getInstagramBestHour()).isEqualTo("8:00 PM");
    }

    @Test
    void analyzeBestPostingTimes_filtersByLast60Days() {
        postCommentRepository.save(PostComment.builder()
                .commentText("Old comment").authorName("User")
                .createdAt(LocalDateTime.now().minusDays(90))
                .post(post).build());

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        assertThat(result.getFacebookTotalComments()).isZero();
    }

    @Test
    void analyzeBestPostingTimes_hourlyDistribution_mapsCorrectly() {
        for (int i = 0; i < 5; i++) {
            postCommentRepository.save(PostComment.builder()
                    .commentText("Comment " + i).authorName("U" + i)
                    .createdAt(LocalDateTime.now().withHour(15).minusDays(i))
                    .post(post).build());
        }

        TimingAnalysisDTO result = postTimingService.analyzeBestPostingTimes();

        Map<Integer, Integer> hourly = result.getHourlyDistribution();
        assertThat(hourly).containsKey(15);
        assertThat(hourly.get(15)).isEqualTo(5);
    }
}
