package com.example.metatry.Services.strategy;

import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.Models.MarketingStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MarketingStrategyMapper {

    private final ObjectMapper objectMapper;

    public MarketingStrategyDTO toDTO(MarketingStrategy entity) {
        if (entity == null) return null;

        int campaignCount = 0;
        Integer estimatedWeeklyPosts = null;
        Integer estimatedTotalPosts = null;
        List<Map<String, Object>> campaignPlanPreviews = null;

        if (entity.getCampaignPlans() != null && !entity.getCampaignPlans().isBlank()) {
            try {
                List<Map<String, Object>> plans = objectMapper.readValue(
                        entity.getCampaignPlans(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                campaignCount = plans.size();
                campaignPlanPreviews = plans;
                int weeklySum = 0;
                for (Map<String, Object> plan : plans) {
                    Object dist = plan.get("weeklyPostDistribution");
                    if (dist instanceof Map) {
                        for (Object val : ((Map<String, Object>) dist).values()) {
                            if (val instanceof Number) {
                                weeklySum += ((Number) val).intValue();
                            }
                        }
                    }
                }
                estimatedWeeklyPosts = weeklySum;
                if (entity.getDurationWeeks() != null) {
                    estimatedTotalPosts = weeklySum * entity.getDurationWeeks();
                }
            } catch (Exception ignored) {}
        }

        return MarketingStrategyDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .aiReasoning(entity.getAiReasoning())
                .durationWeeks(entity.getDurationWeeks())
                .startDate(entity.getStartDate())
                .expectedEndDate(entity.getExpectedEndDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvedAt(entity.getApprovedAt())
                .createdByAI(entity.getCreatedByAI())
                .approved(entity.getApproved())
                .managerNotes(entity.getManagerNotes())
                .lastWeeklyGeneration(entity.getLastWeeklyGeneration())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .campaignCount(campaignCount)
                .autoGenerate(entity.getAutoGenerate())
                .campaignPlans(entity.getCampaignPlans())
                .estimatedWeeklyPosts(estimatedWeeklyPosts)
                .estimatedTotalPosts(estimatedTotalPosts)
                .campaignPlanPreviews(campaignPlanPreviews)
                .build();
    }
}