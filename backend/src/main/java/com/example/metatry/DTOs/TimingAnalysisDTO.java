package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimingAnalysisDTO {

    private String facebookBestHour;
    private String instagramBestHour;
    private int facebookTotalComments;
    private int instagramTotalComments;
    private Map<Integer, Integer> hourlyDistribution;
    private Map<Integer, Integer> dailyDistribution;
    private String recommendation;
}