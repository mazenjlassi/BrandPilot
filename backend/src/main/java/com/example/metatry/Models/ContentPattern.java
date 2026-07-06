package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_patterns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "topic")
    private String topic;

    @Column(name = "campaign_name", length = 255)
    private String campaignName;

    @Column(name = "platform_breakdown", columnDefinition = "TEXT")
    private String platformBreakdown;

    @Column(name = "used_post_ids", columnDefinition = "TEXT")
    private String usedPostIds;

    @Column(name = "post_frequency", length = 100)
    private String postFrequency;

    @Column(name = "content_length", length = 100)
    private String contentLength;

    @Column(name = "media_type", length = 100)
    private String mediaType;

    @Column(name = "hashtag_count", length = 100)
    private String hashtagCount;

    @Column(name = "timing_pattern", length = 200)
    private String timingPattern;

    @Column(name = "tone", length = 255)
    private String tone;

    @Column(name = "cta_style", length = 500)
    private String ctaStyle;

    @Column(name = "total_posts_analyzed")
    private Integer totalPostsAnalyzed;

    @Column(name = "ai_analysis_raw", columnDefinition = "TEXT")
    private String aiAnalysisRaw;

    @Column(name = "avg_engagement_score")
    private Double avgEngagementScore;

    @Column(name = "total_posts_generated")
    private Integer totalPostsGenerated;

    @Column(name = "performance_advice", columnDefinition = "TEXT")
    private String performanceAdvice;

    @Column(name = "last_performance_update")
    private LocalDateTime lastPerformanceUpdate;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    public void onCreate() {
        extractedAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}