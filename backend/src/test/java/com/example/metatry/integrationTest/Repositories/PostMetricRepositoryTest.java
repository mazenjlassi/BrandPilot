package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PostMetricRepositoryTest {

    @Autowired private PostMetricRepository postMetricRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CampaignRepository campaignRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        postMetricRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        Campaign campaign = campaignRepository.save(Campaign.builder().name("Test").topic("AI").build());
        post = postRepository.save(Post.builder()
                .title("Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .campaign(campaign).build());
        postMetricRepository.save(PostMetric.builder()
                .likes(100).comments(10).post(post).build());
    }

    @Test
    void findByPostIdOrderByCollectedAtAsc() {
        assertThat(postMetricRepository.findByPostIdOrderByCollectedAtAsc(post.getId())).hasSize(1);
    }

    @Test
    void findTopByPostIdOrderByCollectedAtDesc() {
        assertThat(postMetricRepository.findTopByPostIdOrderByCollectedAtDesc(post.getId())).isPresent();
    }
}
