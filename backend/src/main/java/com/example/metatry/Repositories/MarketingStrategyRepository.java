package com.example.metatry.Repositories;

import com.example.metatry.Enums.MarketingStrategyStatus;
import com.example.metatry.Models.MarketingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketingStrategyRepository extends JpaRepository<MarketingStrategy, Long> {

    Optional<MarketingStrategy> findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus status);

    List<MarketingStrategy> findAllByOrderByCreatedAtDesc();

    boolean existsByStatus(MarketingStrategyStatus status);

    List<MarketingStrategy> findByStatus(MarketingStrategyStatus status);
}
