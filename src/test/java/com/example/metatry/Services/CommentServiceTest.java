package com.example.metatry.Services;

import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostCommentRepository commentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository);
    }

    @Test
    void getCommentsByPost_delegatesToRepository() {
        List<PostComment> expected = List.of(PostComment.builder().id(1L).build());
        when(commentRepository.findByPostId(10L)).thenReturn(expected);

        List<PostComment> result = commentService.getCommentsByPost(10L);

        assertThat(result).hasSize(1);
        verify(commentRepository).findByPostId(10L);
    }

    @Test
    void getCommentsByCampaign_delegatesToRepository() {
        when(commentRepository.findByPostCampaignId(5L)).thenReturn(List.of());

        commentService.getCommentsByCampaign(5L);

        verify(commentRepository).findByPostCampaignId(5L);
    }

    @Test
    void getCommentsByPostAndSentiment_delegatesToRepository() {
        when(commentRepository.findByPostIdAndSentiment(10L, "positive")).thenReturn(List.of());

        commentService.getCommentsByPostAndSentiment(10L, "positive");

        verify(commentRepository).findByPostIdAndSentiment(10L, "positive");
    }

    @Test
    void save_delegatesToRepository() {
        PostComment comment = PostComment.builder().commentText("Great post!").build();
        when(commentRepository.save(comment)).thenReturn(comment);

        PostComment result = commentService.save(comment);

        assertThat(result).isEqualTo(comment);
        verify(commentRepository).save(comment);
    }
}
