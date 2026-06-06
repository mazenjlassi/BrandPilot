package com.example.metatry.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatternAnalysisRequest {
    private String topic;
    private String platform;
    private Integer minPostsRequired;
    private String companyName;
}