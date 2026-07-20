package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.AiImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyImageDecisionServiceTest {

    @Mock
    private AiImageService aiImageService;

    private WeeklyImageDecisionService service;

    @BeforeEach
    void setUp() {
        service = new WeeklyImageDecisionService(aiImageService);
    }

    @Test
    void decideAndGenerateImages_callsAiForNeedsImageTrue() {
        Post p1 = Post.builder().id(1L).title("P1").platform(PlatformType.LINKEDIN).needsImage(true).build();
        Post p2 = Post.builder().id(2L).title("P2").platform(PlatformType.FACEBOOK).needsImage(false).build();
        Post p3 = Post.builder().id(3L).title("P3").platform(PlatformType.INSTAGRAM).needsImage(true).build();

        List<String> errors = service.decideAndGenerateImages(List.of(p1, p2, p3), "context");

        assertThat(errors).isEmpty();
        verify(aiImageService, times(2)).generateImageForPost(any());
        verify(aiImageService).generateImageForPost(p1);
        verify(aiImageService).generateImageForPost(p3);
        verifyNoMoreInteractions(aiImageService);
    }

    @Test
    void decideAndGenerateImages_skipsWhenNeedsImageNull() {
        Post p1 = Post.builder().id(1L).platform(PlatformType.LINKEDIN).needsImage(null).build();

        List<String> errors = service.decideAndGenerateImages(List.of(p1), "context");

        assertThat(errors).isEmpty();
        verifyNoInteractions(aiImageService);
    }

    @Test
    void decideAndGenerateImages_collectsErrors() {
        Post p1 = Post.builder().id(1L).title("Bad").platform(PlatformType.LINKEDIN).needsImage(true).build();
        doThrow(new RuntimeException("API error")).when(aiImageService).generateImageForPost(p1);

        List<String> errors = service.decideAndGenerateImages(List.of(p1), "context");

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("API error");
    }

    @Test
    void decideAndGenerateImages_skipsNullPlatform() {
        Post p1 = Post.builder().id(1L).platform(null).needsImage(true).build();

        List<String> errors = service.decideAndGenerateImages(List.of(p1), "context");

        assertThat(errors).isEmpty();
        verifyNoInteractions(aiImageService);
    }
}
