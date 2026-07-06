package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapedPostUnitTest {

    @Test
    void builder_setsAllFields() {
        ScrapedPost post = ScrapedPost.builder()
                .id(1L)
                .companyName("Acme")
                .platform("LINKEDIN")
                .postText("Exciting news!")
                .postUrl("https://linkedin.com/posts/1")
                .postedAt("2026-05-01")
                .topic("AI")
                .usedForPattern(true)
                .build();

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getCompanyName()).isEqualTo("Acme");
        assertThat(post.getPlatform()).isEqualTo("LINKEDIN");
        assertThat(post.getPostText()).isEqualTo("Exciting news!");
        assertThat(post.getPostUrl()).isEqualTo("https://linkedin.com/posts/1");
        assertThat(post.getPostedAt()).isEqualTo("2026-05-01");
        assertThat(post.getTopic()).isEqualTo("AI");
        assertThat(post.getUsedForPattern()).isTrue();
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        ScrapedPost post = new ScrapedPost();
        assertThat(post.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        ScrapedPost post = new ScrapedPost(
                1L, "Company", "FACEBOOK", "text",
                "url", "2026-05-01", null, "topic", true
        );

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getCompanyName()).isEqualTo("Company");
        assertThat(post.getPlatform()).isEqualTo("FACEBOOK");
        assertThat(post.getUsedForPattern()).isTrue();
    }

    @Test
    void prePersist_setsDefaults() {
        ScrapedPost post = new ScrapedPost();
        post.onCreate();

        assertThat(post.getScrapedAt()).isNotNull();
        assertThat(post.getUsedForPattern()).isFalse();
    }

    @Test
    void prePersist_doesNotOverrideUsedForPattern() {
        ScrapedPost post = ScrapedPost.builder().usedForPattern(true).build();
        post.onCreate();

        assertThat(post.getUsedForPattern()).isTrue();
    }

    @Test
    void setters_updateFields() {
        ScrapedPost post = new ScrapedPost();

        post.setCompanyName("Updated Corp");
        post.setPlatform("INSTAGRAM");
        post.setUsedForPattern(true);

        assertThat(post.getCompanyName()).isEqualTo("Updated Corp");
        assertThat(post.getPlatform()).isEqualTo("INSTAGRAM");
        assertThat(post.getUsedForPattern()).isTrue();
    }

    @Test
    void nullFields_areHandled() {
        ScrapedPost post = ScrapedPost.builder().build();

        assertThat(post.getId()).isNull();
        assertThat(post.getCompanyName()).isNull();
        assertThat(post.getPlatform()).isNull();
        assertThat(post.getPostText()).isNull();
        assertThat(post.getPostUrl()).isNull();
        assertThat(post.getTopic()).isNull();
    }

    @Test
    void usedForPattern_defaultFalse() {
        ScrapedPost post = new ScrapedPost();
        assertThat(post.getUsedForPattern()).isNull();
        post.onCreate();
        assertThat(post.getUsedForPattern()).isFalse();
    }
}
