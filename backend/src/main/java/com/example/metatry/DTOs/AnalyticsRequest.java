package com.example.metatry.DTOs;

import lombok.Data;

@Data
public class AnalyticsRequest {

    private Long postId;

    private Integer likes;

    private Integer comments;

    private Integer shares;

    private Integer impressions;

}