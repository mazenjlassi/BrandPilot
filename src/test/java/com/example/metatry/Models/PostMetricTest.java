package com.example.metatry.Models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMetricTest {

    @Test
    void builder_setsAllFields() {
        Post post = Post.builder().id(1L).build();
        LocalDateTime now = LocalDateTime.now();

        PostMetric metric = PostMetric.builder()
                .id(1L)
                .likes(150)
                .comments(25)
                .shares(10)
                .impressions(5000)
                .collectedAt(now)
                .post(post)
                .build();

        assertThat(metric.getId()).isEqualTo(1L);
        assertThat(metric.getLikes()).isEqualTo(150);
        assertThat(metric.getComments()).isEqualTo(25);
        assertThat(metric.getShares()).isEqualTo(10);
        assertThat(metric.getImpressions()).isEqualTo(5000);
        assertThat(metric.getCollectedAt()).isEqualTo(now);
        assertThat(metric.getPost()).isSameAs(post);
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        PostMetric metric = new PostMetric();
        assertThat(metric.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Post post = Post.builder().id(2L).build();
        PostMetric metric = new PostMetric(1L, 100, 20, 5, 2000, null, post);

        assertThat(metric.getId()).isEqualTo(1L);
        assertThat(metric.getLikes()).isEqualTo(100);
        assertThat(metric.getComments()).isEqualTo(20);
        assertThat(metric.getShares()).isEqualTo(5);
        assertThat(metric.getImpressions()).isEqualTo(2000);
        assertThat(metric.getPost()).isSameAs(post);
    }

    @Test
    void setters_updateFields() {
        PostMetric metric = new PostMetric();

        metric.setLikes(200);
        metric.setComments(50);
        metric.setShares(25);
        metric.setImpressions(10000);

        assertThat(metric.getLikes()).isEqualTo(200);
        assertThat(metric.getComments()).isEqualTo(50);
        assertThat(metric.getShares()).isEqualTo(25);
        assertThat(metric.getImpressions()).isEqualTo(10000);
    }

    @Test
    void relationship_post() {
        Post post = Post.builder().id(5L).build();
        PostMetric metric = PostMetric.builder().post(post).build();

        assertThat(metric.getPost()).isSameAs(post);
        assertThat(metric.getPost().getId()).isEqualTo(5L);
    }

    @Test
    void nullFields_areHandled() {
        PostMetric metric = PostMetric.builder().build();

        assertThat(metric.getId()).isNull();
        assertThat(metric.getLikes()).isNull();
        assertThat(metric.getComments()).isNull();
        assertThat(metric.getShares()).isNull();
        assertThat(metric.getImpressions()).isNull();
        assertThat(metric.getPost()).isNull();
    }

    @Test
    void zeroValues_areValid() {
        PostMetric metric = PostMetric.builder()
                .likes(0).comments(0).shares(0).impressions(0)
                .build();

        assertThat(metric.getLikes()).isZero();
        assertThat(metric.getComments()).isZero();
        assertThat(metric.getShares()).isZero();
        assertThat(metric.getImpressions()).isZero();
    }
}
