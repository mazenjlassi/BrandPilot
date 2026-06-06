package com.example.metatry;

import com.example.metatry.Enums.MessageRole;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.*;
import com.example.metatry.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RepositoriesTest {

    @Autowired private ContentPatternRepository contentPatternRepository;
    @Autowired private ScrapedPostRepository scrapedPostRepository;
    @Autowired private CompanyProfileRepository companyProfileRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private PostCommentRepository postCommentRepository;
    @Autowired private PostMetricRepository postMetricRepository;
    @Autowired private PostImageRepository postImageRepository;
    @Autowired private MarketingInsightRepository marketingInsightRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CampaignRepository campaignRepository;

    private Campaign campaign;
    private Post post;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        scrapedPostRepository.deleteAll();
        contentPatternRepository.deleteAll();
        companyProfileRepository.deleteAll();
        userRepository.deleteAll();
        conversationRepository.deleteAll();
        messageRepository.deleteAll();
        postCommentRepository.deleteAll();
        postMetricRepository.deleteAll();
        marketingInsightRepository.deleteAll();

        campaign = campaignRepository.save(Campaign.builder().name("Test").topic("AI").build());
        post = postRepository.save(Post.builder()
                .title("Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .campaign(campaign).build());

        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI Technology").companyName("Acme")
                .platformBreakdown("{\"LINKEDIN\":4}").build());

        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("Acme").platform("LINKEDIN")
                .postText("Great post!").postUrl("https://li.com/p1")
                .topic("AI").usedForPattern(false).build());

        companyProfileRepository.save(CompanyProfile.builder()
                .companyName("Acme")
                .instagramUrl("https://ig.com/acme")
                .facebookUrl("https://fb.com/acme")
                .linkedinUrl("https://li.com/acme").build());

        userRepository.save(new User("John", "john@test.com", "pass", Role.ADMIN));

        Conversation conv = conversationRepository.save(
                Conversation.builder().title("Chat").conclusion("Summary").build());
        messageRepository.save(Message.builder()
                .role(MessageRole.USER).content("Hello").conversation(conv).build());

        postCommentRepository.save(PostComment.builder()
                .commentText("Great!").sentiment("POSITIVE")
                .externalCommentId("ext_1").post(post).build());

        postMetricRepository.save(PostMetric.builder()
                .likes(100).comments(10).post(post).build());

        marketingInsightRepository.save(MarketingInsight.builder()
                .platform("LINKEDIN").insightType("ENGAGEMENT")
                .description("High").confidenceScore(0.85).build());
    }

    // ========== ContentPatternRepository ==========

    @Test
    void contentPattern_findByTopic() {
        assertThat(contentPatternRepository.findByTopic("AI Technology")).isPresent();
    }

    @Test
    void contentPattern_findByTopicContainingIgnoreCase() {
        assertThat(contentPatternRepository.findByTopicContainingIgnoreCase("ai")).isNotEmpty();
    }

    @Test
    void contentPattern_findByCompanyName() {
        assertThat(contentPatternRepository.findByCompanyName("Acme")).hasSize(1);
    }

    @Test
    void contentPattern_findTop3ByOrderByExtractedAtDesc() {
        assertThat(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).isNotEmpty();
    }

    // ========== ScrapedPostRepository ==========

    @Test
    void scrapedPost_findByCompanyNameAndPlatformAndPostUrl() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatformAndPostUrl(
                "Acme", "LINKEDIN", "https://li.com/p1")).isPresent();
    }

    @Test
    void scrapedPost_findByCompanyNameAndPlatformAndPostText() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatformAndPostText(
                "Acme", "LINKEDIN", "Great post!")).isPresent();
    }

    @Test
    void scrapedPost_findByCompanyName() {
        assertThat(scrapedPostRepository.findByCompanyName("Acme")).hasSize(1);
    }

    @Test
    void scrapedPost_findByPlatform() {
        assertThat(scrapedPostRepository.findByPlatform("LINKEDIN")).hasSize(1);
    }

    @Test
    void scrapedPost_findByTopic() {
        assertThat(scrapedPostRepository.findByTopic("AI")).hasSize(1);
    }

    @Test
    void scrapedPost_findByCompanyNameAndPlatform() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatform("Acme", "LINKEDIN")).hasSize(1);
    }

    @Test
    void scrapedPost_findByUsedForPatternFalse() {
        assertThat(scrapedPostRepository.findByUsedForPatternFalse()).isNotEmpty();
    }

    @Test
    void scrapedPost_countByUsedForPatternFalse() {
        assertThat(scrapedPostRepository.countByUsedForPatternFalse()).isPositive();
    }

    @Test
    void scrapedPost_countByCompanyName() {
        assertThat(scrapedPostRepository.countByCompanyName("Acme")).isPositive();
    }

    @Test
    void scrapedPost_findDistinctCompanyNames() {
        assertThat(scrapedPostRepository.findDistinctCompanyNames()).contains("Acme");
    }

    // ========== CompanyProfileRepository ==========

    @Test
    void companyProfile_findByCompanyName() {
        assertThat(companyProfileRepository.findByCompanyName("Acme")).isPresent();
    }

    @Test
    void companyProfile_existsByCompanyName() {
        assertThat(companyProfileRepository.existsByCompanyName("Acme")).isTrue();
    }

    // ========== UserRepository ==========

    @Test
    void user_findByName() {
        Optional<User> user = userRepository.findByName("John");
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void user_findByEmail() {
        assertThat(userRepository.findByEmail("john@test.com")).isPresent();
    }

    // ========== CampaignRepository ==========

    @Test
    void campaign_findAllByOrderByCreatedAtDesc() {
        assertThat(campaignRepository.findAllByOrderByCreatedAtDesc()).isNotEmpty();
    }

    @Test
    void campaign_findAllWithPosts() {
        assertThat(campaignRepository.findAllWithPosts()).isNotEmpty();
    }

    // ========== ConversationRepository ==========

    @Test
    void conversation_findTop2ByConclusionIsNotNull() {
        List<Conversation> convs = conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc();
        assertThat(convs).isNotEmpty();
        assertThat(convs.get(0).getConclusion()).isNotNull();
    }

    // ========== MessageRepository ==========

    @Test
    void message_findByConversationIdOrderByTimestampAsc() {
        Long convId = conversationRepository.findAll().get(0).getId();
        assertThat(messageRepository.findByConversationIdOrderByTimestampAsc(convId)).isNotEmpty();
    }

    @Test
    void message_findTop2ByRoleOrderByTimestampDesc() {
        assertThat(messageRepository.findTop2ByRoleOrderByTimestampDesc(MessageRole.USER)).isNotEmpty();
    }

    // ========== PostCommentRepository ==========

    @Test
    void postComment_findByPostId() {
        assertThat(postCommentRepository.findByPostId(post.getId())).hasSize(1);
    }

    @Test
    void postComment_findByPostIdAndSentiment() {
        assertThat(postCommentRepository.findByPostIdAndSentiment(post.getId(), "POSITIVE")).hasSize(1);
    }

    @Test
    void postComment_countByPostId() {
        assertThat(postCommentRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    void postComment_existsByExternalCommentId() {
        assertThat(postCommentRepository.existsByExternalCommentId("ext_1")).isTrue();
    }

    @Test
    void postComment_findByPlatformAndCreatedAtAfter() {
        assertThat(postCommentRepository.findByPlatformAndCreatedAtAfter(
                PlatformType.LINKEDIN, LocalDateTime.now().minusDays(1))).isNotEmpty();
    }

    // ========== PostMetricRepository ==========

    @Test
    void postMetric_findByPostIdOrderByCollectedAtAsc() {
        assertThat(postMetricRepository.findByPostIdOrderByCollectedAtAsc(post.getId())).hasSize(1);
    }

    @Test
    void postMetric_findTopByPostIdOrderByCollectedAtDesc() {
        assertThat(postMetricRepository.findTopByPostIdOrderByCollectedAtDesc(post.getId())).isPresent();
    }

    // ========== PostImageRepository ==========

    @Test
    void postImage_saveAndFind() {
        PostImage image = postImageRepository.save(
                PostImage.builder().imageUrl("https://img.com/img.jpg").post(post).build());
        assertThat(postImageRepository.findById(image.getId())).isPresent();
    }

    // ========== MarketingInsightRepository ==========

    @Test
    void marketingInsight_findTop5ByOrderByConfidenceScoreDesc() {
        assertThat(marketingInsightRepository.findTop5ByOrderByConfidenceScoreDesc()).isNotEmpty();
    }

    @Test
    void marketingInsight_findByPlatform() {
        assertThat(marketingInsightRepository.findByPlatform("LINKEDIN")).hasSize(1);
    }

    @Test
    void marketingInsight_findTop2ByOrderByCreatedAtDesc() {
        assertThat(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).isNotEmpty();
    }
}
