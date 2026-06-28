package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
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
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;
    @Autowired private CampaignRepository campaignRepository;

    private Post savedPost;
    private Campaign savedCampaign;

    @BeforeEach
    void setUp() {
        campaignRepository.deleteAll();
        postRepository.deleteAll();

        savedCampaign = campaignRepository.save(
                Campaign.builder().name("Test Campaign").topic("AI").build());

        savedPost = postRepository.save(Post.builder()
                .title("Test Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .approved(true).campaign(savedCampaign)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build());
    }

    @Test
    void findByIdWithDetails() {
        Optional<Post> found = postRepository.findByIdWithDetails(savedPost.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Post");
    }

    @Test
    void findByApprovedTrue() {
        assertThat(postRepository.findByApprovedTrue()).isNotEmpty()
                .allMatch(p -> Boolean.TRUE.equals(p.getApproved()));
    }

    @Test
    void findByStatus() {
        assertThat(postRepository.findByStatus(PostStatus.DRAFT)).isNotEmpty()
                .allMatch(p -> p.getStatus() == PostStatus.DRAFT);
    }

    @Test
    void findByPlatform() {
        assertThat(postRepository.findByPlatform(PlatformType.LINKEDIN)).isNotEmpty()
                .allMatch(p -> p.getPlatform() == PlatformType.LINKEDIN);
    }

    @Test
    void countByStatus() {
        assertThat(postRepository.countByStatus(PostStatus.DRAFT)).isPositive();
    }

    @Test
    void countByApprovedTrue() {
        assertThat(postRepository.countByApprovedTrue()).isPositive();
    }

    @Test
    void countByPlatform() {
        assertThat(postRepository.countByPlatform(PlatformType.LINKEDIN)).isPositive();
    }

    @Test
    void findByCampaignId() {
        assertThat(postRepository.findByCampaignId(savedCampaign.getId())).hasSize(1);
    }

    @Test
    void findByCampaignIdAndStatus() {
        assertThat(postRepository.findByCampaignIdAndStatus(savedCampaign.getId(), PostStatus.DRAFT)).hasSize(1);
    }

    @Test
    void findTop20ByStatusOrderByPublishedAtDesc() {
        assertThat(postRepository.findTop20ByStatusOrderByPublishedAtDesc(PostStatus.DRAFT)).isNotNull();
    }

    @Test
    void findByApprovedTrueAndStatusAndScheduledAtBefore() {
        assertThat(postRepository.findByApprovedTrueAndStatusAndScheduledAtBefore(
                PostStatus.DRAFT, LocalDateTime.now().plusDays(2))).isNotEmpty();
    }

    @Test
    void findByPermanentTrue() {
        assertThat(postRepository.findByPermanentTrue()).isEmpty();
    }

    @Test
    void findByStatusAndScheduledAtAfterOrderByScheduledAtAsc() {
        assertThat(postRepository.findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                PostStatus.DRAFT, LocalDateTime.now())).isNotEmpty();
    }

    @Test
    void findByStatusOrderByScheduledAtAsc() {
        assertThat(postRepository.findByStatusOrderByScheduledAtAsc(PostStatus.DRAFT)).isNotEmpty();
    }

    @Test
    void countByStatusAndPublishedAtBetween() {
        assertThat(postRepository.countByStatusAndPublishedAtBetween(
                PostStatus.DRAFT, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2))).isNotNegative();
    }

    @Test
    void findByStatusAndScheduledAtBetween() {
        assertThat(postRepository.findByStatusAndScheduledAtBetween(
                PostStatus.DRAFT, LocalDateTime.now(), LocalDateTime.now().plusDays(2))).isNotEmpty();
    }

    @Test
    void findTop3ByOrderByCreatedAtDesc() {
        assertThat(postRepository.findTop3ByOrderByCreatedAtDesc()).isNotEmpty();
    }
}
