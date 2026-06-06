package com.example.metatry.DTOs;

import lombok.Data;

@Data
public class AnalyticsReport {

    private int totalPosts;

    private double averageEngagement;

    private int positiveComments;

    private int negativeComments;

    private String bestPostingHour;

    private String aiRecommendation;
}