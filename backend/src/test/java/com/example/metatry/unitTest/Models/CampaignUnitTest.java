package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampaignUnitTest {

    @Test
    void builder_setsAllFields() {
        Campaign campaign = Campaign.builder()
                .id(1L)
                .name("Spring Launch")
                .topic("AI in Healthcare")
                .build();

        assertThat(campaign.getId()).isEqualTo(1L);
        assertThat(campaign.getName()).isEqualTo("Spring Launch");
        assertThat(campaign.getTopic()).isEqualTo("AI in Healthcare");
    }

    @Test
    void noArgsConstructor_initializesPostsList() {
        Campaign campaign = new Campaign();
        assertThat(campaign.getPosts()).isNotNull();
        assertThat(campaign.getPosts()).isEmpty();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Post post = Post.builder().title("Test").build();
        List<Post> posts = List.of(post);

        Campaign campaign = new Campaign(1L, "Campaign", "Topic", null, posts);

        assertThat(campaign.getId()).isEqualTo(1L);
        assertThat(campaign.getName()).isEqualTo("Campaign");
        assertThat(campaign.getTopic()).isEqualTo("Topic");
        assertThat(campaign.getPosts()).hasSize(1);
    }

    @Test
    void builderDefault_initializesPostsList() {
        Campaign campaign = Campaign.builder().name("No Posts").build();
        assertThat(campaign.getPosts()).isNotNull();
        assertThat(campaign.getPosts()).isEmpty();
    }

    @Test
    void setters_updateFields() {
        Campaign campaign = new Campaign();

        campaign.setId(5L);
        campaign.setName("Renamed");
        campaign.setTopic("New Topic");

        assertThat(campaign.getId()).isEqualTo(5L);
        assertThat(campaign.getName()).isEqualTo("Renamed");
        assertThat(campaign.getTopic()).isEqualTo("New Topic");
    }

    @Test
    void relationship_posts() {
        Campaign campaign = Campaign.builder().name("Test Campaign").build();
        Post post1 = Post.builder().title("Post 1").build();
        Post post2 = Post.builder().title("Post 2").build();

        campaign.setPosts(List.of(post1, post2));

        assertThat(campaign.getPosts()).hasSize(2);
        assertThat(campaign.getPosts().get(0).getTitle()).isEqualTo("Post 1");
        assertThat(campaign.getPosts().get(1).getTitle()).isEqualTo("Post 2");
    }

    @Test
    void nullFields_areHandled() {
        Campaign campaign = Campaign.builder().build();

        assertThat(campaign.getId()).isNull();
        assertThat(campaign.getName()).isNull();
        assertThat(campaign.getTopic()).isNull();
        assertThat(campaign.getPosts()).isNotNull().isEmpty();
    }

    @Test
    void createdAt_remainsNull_whenNotSet() {
        Campaign campaign = new Campaign();
        assertThat(campaign.getCreatedAt()).isNull();
    }
}
