package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MarketingInsightUnitTest {

    @Test
    void builder_setsAllFields() {
        MarketingInsight insight = MarketingInsight.builder()
                .id(1L)
                .platform("LINKEDIN")
                .insightType("ENGAGEMENT")
                .description("Posts with videos get 2x engagement")
                .confidenceScore(0.85)
                .build();

        assertThat(insight.getId()).isEqualTo(1L);
        assertThat(insight.getPlatform()).isEqualTo("LINKEDIN");
        assertThat(insight.getInsightType()).isEqualTo("ENGAGEMENT");
        assertThat(insight.getDescription()).isEqualTo("Posts with videos get 2x engagement");
        assertThat(insight.getConfidenceScore()).isEqualTo(0.85);
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        MarketingInsight insight = new MarketingInsight();
        assertThat(insight.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        MarketingInsight insight = new MarketingInsight(1L, "FACEBOOK", "TIMING",
                "Best time is 9 AM", 0.72, null);

        assertThat(insight.getId()).isEqualTo(1L);
        assertThat(insight.getPlatform()).isEqualTo("FACEBOOK");
        assertThat(insight.getInsightType()).isEqualTo("TIMING");
        assertThat(insight.getDescription()).isEqualTo("Best time is 9 AM");
        assertThat(insight.getConfidenceScore()).isEqualTo(0.72);
    }

    @Test
    void setters_updateFields() {
        MarketingInsight insight = new MarketingInsight();

        insight.setPlatform("INSTAGRAM");
        insight.setInsightType("HASHTAG");
        insight.setDescription("Use 5-10 hashtags");
        insight.setConfidenceScore(0.9);

        assertThat(insight.getPlatform()).isEqualTo("INSTAGRAM");
        assertThat(insight.getInsightType()).isEqualTo("HASHTAG");
        assertThat(insight.getDescription()).isEqualTo("Use 5-10 hashtags");
        assertThat(insight.getConfidenceScore()).isEqualTo(0.9);
    }

    @Test
    void nullFields_areHandled() {
        MarketingInsight insight = MarketingInsight.builder().build();

        assertThat(insight.getId()).isNull();
        assertThat(insight.getPlatform()).isNull();
        assertThat(insight.getInsightType()).isNull();
        assertThat(insight.getDescription()).isNull();
        assertThat(insight.getConfidenceScore()).isNull();
    }

    @Test
    void confidenceScore_range() {
        MarketingInsight low = MarketingInsight.builder().confidenceScore(0.0).build();
        MarketingInsight high = MarketingInsight.builder().confidenceScore(1.0).build();
        MarketingInsight mid = MarketingInsight.builder().confidenceScore(0.55).build();

        assertThat(low.getConfidenceScore()).isEqualTo(0.0);
        assertThat(high.getConfidenceScore()).isEqualTo(1.0);
        assertThat(mid.getConfidenceScore()).isBetween(0.5, 0.6);
    }

    @Test
    void createdAt_remainsNull_whenNotSet() {
        MarketingInsight insight = new MarketingInsight();
        assertThat(insight.getCreatedAt()).isNull();
    }

    @Test
    void description_longText() {
        String longDesc = "D".repeat(2000);
        MarketingInsight insight = MarketingInsight.builder().description(longDesc).build();

        assertThat(insight.getDescription()).hasSize(2000);
    }
}
