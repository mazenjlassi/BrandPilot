package com.example.metatry.Models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostCommentTest {

    @Test
    void builder_setsAllFields() {
        Post post = Post.builder().id(1L).build();
        LocalDateTime now = LocalDateTime.now();

        PostComment comment = PostComment.builder()
                .id(1L)
                .externalCommentId("fb_comment_123")
                .commentText("Great post!")
                .sentiment("POSITIVE")
                .authorName("John Doe")
                .createdAt(now)
                .post(post)
                .build();

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getExternalCommentId()).isEqualTo("fb_comment_123");
        assertThat(comment.getCommentText()).isEqualTo("Great post!");
        assertThat(comment.getSentiment()).isEqualTo("POSITIVE");
        assertThat(comment.getAuthorName()).isEqualTo("John Doe");
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getPost()).isSameAs(post);
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        PostComment comment = new PostComment();
        assertThat(comment.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Post post = Post.builder().id(2L).build();
        PostComment comment = new PostComment(1L, "ext_1", "Nice!", "POSITIVE", "Alice", null, post);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getExternalCommentId()).isEqualTo("ext_1");
        assertThat(comment.getCommentText()).isEqualTo("Nice!");
        assertThat(comment.getSentiment()).isEqualTo("POSITIVE");
        assertThat(comment.getAuthorName()).isEqualTo("Alice");
        assertThat(comment.getPost()).isSameAs(post);
    }

    @Test
    void setters_updateFields() {
        PostComment comment = new PostComment();

        comment.setCommentText("Updated text");
        comment.setSentiment("NEGATIVE");
        comment.setAuthorName("Jane");
        comment.setExternalCommentId("ext_updated");

        assertThat(comment.getCommentText()).isEqualTo("Updated text");
        assertThat(comment.getSentiment()).isEqualTo("NEGATIVE");
        assertThat(comment.getAuthorName()).isEqualTo("Jane");
        assertThat(comment.getExternalCommentId()).isEqualTo("ext_updated");
    }

    @Test
    void relationship_post() {
        Post post = Post.builder().id(10L).build();
        PostComment comment = PostComment.builder().post(post).build();

        assertThat(comment.getPost()).isSameAs(post);
        assertThat(comment.getPost().getId()).isEqualTo(10L);
    }

    @Test
    void nullFields_areHandled() {
        PostComment comment = PostComment.builder().build();

        assertThat(comment.getId()).isNull();
        assertThat(comment.getCommentText()).isNull();
        assertThat(comment.getSentiment()).isNull();
        assertThat(comment.getAuthorName()).isNull();
        assertThat(comment.getPost()).isNull();
    }

    @Test
    void sentiment_values() {
        PostComment pos = PostComment.builder().sentiment("POSITIVE").build();
        PostComment neg = PostComment.builder().sentiment("NEGATIVE").build();
        PostComment neu = PostComment.builder().sentiment("NEUTRAL").build();

        assertThat(pos.getSentiment()).isEqualTo("POSITIVE");
        assertThat(neg.getSentiment()).isEqualTo("NEGATIVE");
        assertThat(neu.getSentiment()).isEqualTo("NEUTRAL");
    }
}
