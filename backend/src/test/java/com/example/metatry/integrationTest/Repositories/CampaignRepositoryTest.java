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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CampaignRepositoryTest {

    @Autowired private CampaignRepository campaignRepository;
    @Autowired private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        Campaign campaign = campaignRepository.save(Campaign.builder().name("Test").topic("AI").build());
        postRepository.save(Post.builder()
                .title("Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .campaign(campaign).build());
    }

    @Test
    void findAllByOrderByCreatedAtDesc() {
        assertThat(campaignRepository.findAllByOrderByCreatedAtDesc()).isNotEmpty();
    }

    @Test
    void findAllWithPosts() {
        assertThat(campaignRepository.findAllWithPosts()).isNotEmpty();
    }
}
