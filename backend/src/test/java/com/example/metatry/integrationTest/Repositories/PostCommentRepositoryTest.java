package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PostCommentRepositoryTest {

    @Autowired private PostCommentRepository postCommentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CampaignRepository campaignRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        Campaign campaign = campaignRepository.save(Campaign.builder().name("Test").topic("AI").build());
        post = postRepository.save(Post.builder()
                .title("Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .campaign(campaign).build());
        postCommentRepository.save(PostComment.builder()
                .commentText("Great!").sentiment("POSITIVE")
                .externalCommentId("ext_1").post(post)
                .createdAt(LocalDateTime.now()).build());
    }

    @Test
    void findByPostId() {
        assertThat(postCommentRepository.findByPostId(post.getId())).hasSize(1);
    }

    @Test
    void findByPostIdAndSentiment() {
        assertThat(postCommentRepository.findByPostIdAndSentiment(post.getId(), "POSITIVE")).hasSize(1);
    }

    @Test
    void countByPostId() {
        assertThat(postCommentRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    void existsByExternalCommentId() {
        assertThat(postCommentRepository.existsByExternalCommentId("ext_1")).isTrue();
    }

    @Test
    void findByPlatformAndCreatedAtAfter() {
        assertThat(postCommentRepository.findByPlatformAndCreatedAtAfter(
                PlatformType.LINKEDIN, LocalDateTime.now().minusDays(1))).isNotEmpty();
    }
}
