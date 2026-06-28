package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostImageRepository;
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
class PostImageRepositoryTest {

    @Autowired private PostImageRepository postImageRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CampaignRepository campaignRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        Campaign campaign = campaignRepository.save(Campaign.builder().name("Test").topic("AI").build());
        post = postRepository.save(Post.builder()
                .title("Post").content("Content")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .campaign(campaign).build());
    }

    @Test
    void saveAndFind() {
        PostImage image = postImageRepository.save(
                PostImage.builder().imageUrl("https://img.com/img.jpg").post(post).build());
        assertThat(postImageRepository.findById(image.getId())).isPresent();
    }
}
