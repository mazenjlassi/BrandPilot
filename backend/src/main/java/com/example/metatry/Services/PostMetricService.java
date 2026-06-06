package com.example.metatry.Services;

import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostMetricService {

    private final PostMetricRepository postMetricRepository;

    /**
     * Get full metrics history for a post (for charts)
     */
    public List<PostMetric> getMetricsHistory(Long postId) {
        return postMetricRepository.findByPostIdOrderByCollectedAtAsc(postId);
    }

    /**
     *  Get latest metrics snapshot
     */
    public PostMetric getLatestMetric(Long postId) {
        return postMetricRepository
                .findTopByPostIdOrderByCollectedAtDesc(postId)
                .orElse(null);
    }

    /**
     * 📈 Get max stats (best performance)
     */
    public int getMaxLikes(Long postId) {
        return getMetricsHistory(postId)
                .stream()
                .mapToInt(m -> m.getLikes() != null ? m.getLikes() : 0)
                .max()
                .orElse(0);
    }

    public int getMaxComments(Long postId) {
        return getMetricsHistory(postId)
                .stream()
                .mapToInt(m -> m.getComments() != null ? m.getComments() : 0)
                .max()
                .orElse(0);
    }

    public int getMaxShares(Long postId) {
        return getMetricsHistory(postId)
                .stream()
                .mapToInt(m -> m.getShares() != null ? m.getShares() : 0)
                .max()
                .orElse(0);
    }
}