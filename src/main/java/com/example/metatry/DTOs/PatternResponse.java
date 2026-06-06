package com.example.metatry.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatternResponse {
    private Long id;
    private String topic;
    private String campaignName;
    private String platformBreakdown;
    private String postFrequency;
    private String contentLength;
    private String mediaType;
    private String hashtagCount;
    private String timingPattern;
    private String tone;
    private String ctaStyle;
    private Integer totalPostsAnalyzed;
    private LocalDateTime extractedAt;
    private String status;
    private String message;
}