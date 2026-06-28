package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Models.MarketingInsight;
import com.example.metatry.Repositories.MarketingInsightRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MarketingInsightRepositoryTest {

    @Autowired private MarketingInsightRepository marketingInsightRepository;

    @BeforeEach
    void setUp() {
        marketingInsightRepository.deleteAll();
        marketingInsightRepository.save(MarketingInsight.builder()
                .platform("LINKEDIN").insightType("ENGAGEMENT")
                .description("High").confidenceScore(0.85).build());
    }

    @Test
    void findTop5ByOrderByConfidenceScoreDesc() {
        assertThat(marketingInsightRepository.findTop5ByOrderByConfidenceScoreDesc()).isNotEmpty();
    }

    @Test
    void findByPlatform() {
        assertThat(marketingInsightRepository.findByPlatform("LINKEDIN")).hasSize(1);
    }

    @Test
    void findTop2ByOrderByCreatedAtDesc() {
        assertThat(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).isNotEmpty();
    }
}
