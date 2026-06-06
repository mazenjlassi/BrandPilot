package com.example.metatry.Repositories;

import com.example.metatry.Models.MarketingInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketingInsightRepository extends JpaRepository<MarketingInsight, Long> {

    List<MarketingInsight> findTop5ByOrderByConfidenceScoreDesc();

    List<MarketingInsight> findByPlatform(String platform);

    List<MarketingInsight> findTop2ByOrderByCreatedAtDesc();
}