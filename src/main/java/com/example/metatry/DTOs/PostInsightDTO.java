package com.example.metatry.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostInsightDTO {

    private String overallSentiment;

    private double positiveRatio;
    private double negativeRatio;
    private double neutralRatio;

    private List<String> topComplaints;
    private List<String> topPositives;
    private List<String> topNeutral;

    private String summary;
    private String advice;
    private List<String> ideas;
}