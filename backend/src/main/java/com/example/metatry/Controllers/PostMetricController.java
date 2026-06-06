package com.example.metatry.Controllers;

import com.example.metatry.Models.PostMetric;
import com.example.metatry.Services.PostMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class PostMetricController {

    private final PostMetricService postMetricService;

    /**
     * 📊 Full history (for charts)
     */
    @GetMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostMetric>> getHistory(@PathVariable Long postId) {

        return ResponseEntity.ok(
                postMetricService.getMetricsHistory(postId)
        );
    }

    /**
     * 📌 Latest metrics snapshot
     */
    @GetMapping("/post/{postId}/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostMetric> getLatest(@PathVariable Long postId) {

        return ResponseEntity.ok(
                postMetricService.getLatestMetric(postId)
        );
    }

    /**
     * 📈 Best performance stats
     */
    @GetMapping("/post/{postId}/max")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Integer>> getMax(@PathVariable Long postId) {

        return ResponseEntity.ok(Map.of(
                "likes", postMetricService.getMaxLikes(postId),
                "comments", postMetricService.getMaxComments(postId),
                "shares", postMetricService.getMaxShares(postId)
        ));
    }
}