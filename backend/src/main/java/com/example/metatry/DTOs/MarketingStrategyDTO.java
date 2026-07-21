package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingStrategyDTO {

    private Long id;
    private String title;
    private String summary;
    private String description;
    private String aiReasoning;
    private Integer durationWeeks;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private Boolean createdByAI;
    private Boolean approved;
    private String managerNotes;
    private LocalDate lastWeeklyGeneration;
    private String status;
    private int campaignCount;
    private Boolean autoGenerate;
    private String campaignPlans;
    private Integer estimatedWeeklyPosts;
    private Integer estimatedTotalPosts;
    private List<Map<String, Object>> campaignPlanPreviews;
}
