package com.example.metatry.Models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ContentPatternTest {

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        ContentPattern pattern = ContentPattern.builder()
                .id(1L)
                .companyName("Acme Corp")
                .topic("AI Technology")
                .campaignName("Q1 Launch")
                .platformBreakdown("{\"LINKEDIN\": 4, \"FACEBOOK\": 2}")
                .usedPostIds("1,2,3")
                .postFrequency("daily")
                .contentLength("medium")
                .mediaType("image")
                .hashtagCount("3-5")
                .timingPattern("morning")
                .tone("professional")
                .ctaStyle("learn more")
                .totalPostsAnalyzed(50)
                .aiAnalysisRaw("raw analysis text")
                .avgEngagementScore(0.75)
                .totalPostsGenerated(10)
                .performanceAdvice("Post more videos")
                .lastPerformanceUpdate(now)
                .extractedAt(now)
                .lastUpdatedAt(now)
                .build();

        assertThat(pattern.getId()).isEqualTo(1L);
        assertThat(pattern.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(pattern.getTopic()).isEqualTo("AI Technology");
        assertThat(pattern.getPlatformBreakdown()).isEqualTo("{\"LINKEDIN\": 4, \"FACEBOOK\": 2}");
        assertThat(pattern.getUsedPostIds()).isEqualTo("1,2,3");
        assertThat(pattern.getPostFrequency()).isEqualTo("daily");
        assertThat(pattern.getContentLength()).isEqualTo("medium");
        assertThat(pattern.getMediaType()).isEqualTo("image");
        assertThat(pattern.getHashtagCount()).isEqualTo("3-5");
        assertThat(pattern.getTimingPattern()).isEqualTo("morning");
        assertThat(pattern.getTone()).isEqualTo("professional");
        assertThat(pattern.getCtaStyle()).isEqualTo("learn more");
        assertThat(pattern.getTotalPostsAnalyzed()).isEqualTo(50);
        assertThat(pattern.getAiAnalysisRaw()).isEqualTo("raw analysis text");
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.75);
        assertThat(pattern.getTotalPostsGenerated()).isEqualTo(10);
        assertThat(pattern.getPerformanceAdvice()).isEqualTo("Post more videos");
        assertThat(pattern.getLastPerformanceUpdate()).isEqualTo(now);
        assertThat(pattern.getExtractedAt()).isEqualTo(now);
        assertThat(pattern.getLastUpdatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        ContentPattern pattern = new ContentPattern();
        assertThat(pattern.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        ContentPattern pattern = new ContentPattern(
                1L, "Company", "Topic", "Campaign",
                "{}", "1,2", "weekly", "short",
                "video", "1-3", "evening", "casual",
                "subscribe", 100, "raw", 0.5,
                5, "advice", now, now, now
        );

        assertThat(pattern.getId()).isEqualTo(1L);
        assertThat(pattern.getCompanyName()).isEqualTo("Company");
        assertThat(pattern.getTopic()).isEqualTo("Topic");
        assertThat(pattern.getCampaignName()).isEqualTo("Campaign");
        assertThat(pattern.getTotalPostsAnalyzed()).isEqualTo(100);
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.5);
    }

    @Test
    void prePersist_setsTimestamps() {
        ContentPattern pattern = new ContentPattern();
        pattern.onCreate();

        assertThat(pattern.getExtractedAt()).isNotNull();
        assertThat(pattern.getLastUpdatedAt()).isNotNull();
        assertThat(pattern.getExtractedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(pattern.getLastUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void preUpdate_updatesLastUpdatedAt() {
        ContentPattern pattern = new ContentPattern();
        pattern.onCreate();
        LocalDateTime original = pattern.getLastUpdatedAt();

        pattern.onUpdate();

        assertThat(pattern.getLastUpdatedAt()).isAfterOrEqualTo(original);
    }

    @Test
    void setters_updateFields() {
        ContentPattern pattern = new ContentPattern();

        pattern.setCompanyName("New Corp");
        pattern.setTopic("New Topic");
        pattern.setTone("exciting");
        pattern.setAvgEngagementScore(0.9);

        assertThat(pattern.getCompanyName()).isEqualTo("New Corp");
        assertThat(pattern.getTopic()).isEqualTo("New Topic");
        assertThat(pattern.getTone()).isEqualTo("exciting");
        assertThat(pattern.getAvgEngagementScore()).isEqualTo(0.9);
    }

    @Test
    void nullFields_areHandled() {
        ContentPattern pattern = ContentPattern.builder().build();

        assertThat(pattern.getId()).isNull();
        assertThat(pattern.getCompanyName()).isNull();
        assertThat(pattern.getAvgEngagementScore()).isNull();
        assertThat(pattern.getTotalPostsAnalyzed()).isNull();
        assertThat(pattern.getTotalPostsGenerated()).isNull();
    }

    @Test
    void score_positiveValues() {
        ContentPattern pattern = ContentPattern.builder().avgEngagementScore(0.95).build();
        assertThat(pattern.getAvgEngagementScore()).isGreaterThan(0.9);
    }

    @Test
    void stringFields_areEmptyByDefault() {
        ContentPattern pattern = new ContentPattern();
        assertThat(pattern.getCompanyName()).isNull();
        assertThat(pattern.getPlatformBreakdown()).isNull();
        assertThat(pattern.getPerformanceAdvice()).isNull();
    }
}
