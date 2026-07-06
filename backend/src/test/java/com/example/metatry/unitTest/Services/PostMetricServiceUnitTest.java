package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostMetricServiceUnitTest {

    @Mock
    private PostMetricRepository postMetricRepository;

    private PostMetricService postMetricService;

    @BeforeEach
    void setUp() {
        postMetricService = new PostMetricService(postMetricRepository);
    }

    @Test
    void getMetricsHistory_returnsOrderedMetrics() {
        List<PostMetric> expected = List.of(
                PostMetric.builder().id(1L).likes(10).build(),
                PostMetric.builder().id(2L).likes(20).build()
        );
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(expected);

        List<PostMetric> result = postMetricService.getMetricsHistory(1L);

        assertThat(result).hasSize(2);
        verify(postMetricRepository).findByPostIdOrderByCollectedAtAsc(1L);
    }

    @Test
    void getLatestMetric_whenFound_returnsLatest() {
        PostMetric latest = PostMetric.builder().id(2L).likes(30).build();
        when(postMetricRepository.findTopByPostIdOrderByCollectedAtDesc(1L)).thenReturn(Optional.of(latest));

        PostMetric result = postMetricService.getLatestMetric(1L);

        assertThat(result).isNotNull();
        assertThat(result.getLikes()).isEqualTo(30);
    }

    @Test
    void getLatestMetric_whenNotFound_returnsNull() {
        when(postMetricRepository.findTopByPostIdOrderByCollectedAtDesc(1L)).thenReturn(Optional.empty());

        PostMetric result = postMetricService.getLatestMetric(1L);

        assertThat(result).isNull();
    }

    @Test
    void getMaxLikes_returnsHighestValue() {
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(List.of(
                PostMetric.builder().likes(10).comments(1).shares(0).build(),
                PostMetric.builder().likes(50).comments(5).shares(3).build(),
                PostMetric.builder().likes(25).comments(2).shares(1).build()
        ));
        assertThat(postMetricService.getMaxLikes(1L)).isEqualTo(50);
    }

    @Test
    void getMaxComments_returnsHighestValue() {
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(List.of(
                PostMetric.builder().likes(10).comments(1).shares(0).build(),
                PostMetric.builder().likes(50).comments(8).shares(3).build(),
                PostMetric.builder().likes(25).comments(3).shares(1).build()
        ));
        assertThat(postMetricService.getMaxComments(1L)).isEqualTo(8);
    }

    @Test
    void getMaxShares_returnsHighestValue() {
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(List.of(
                PostMetric.builder().likes(10).comments(1).shares(2).build(),
                PostMetric.builder().likes(50).comments(5).shares(7).build(),
                PostMetric.builder().likes(25).comments(2).shares(4).build()
        ));
        assertThat(postMetricService.getMaxShares(1L)).isEqualTo(7);
    }

    @Test
    void getMaxLikes_whenNoMetrics_returnsZero() {
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(List.of());
        assertThat(postMetricService.getMaxLikes(1L)).isZero();
    }

    @Test
    void getMaxLikes_whenNullValues_returnsZero() {
        when(postMetricRepository.findByPostIdOrderByCollectedAtAsc(1L)).thenReturn(List.of(
                PostMetric.builder().likes(null).comments(null).shares(null).build()
        ));
        assertThat(postMetricService.getMaxLikes(1L)).isZero();
        assertThat(postMetricService.getMaxComments(1L)).isZero();
        assertThat(postMetricService.getMaxShares(1L)).isZero();
    }
}
