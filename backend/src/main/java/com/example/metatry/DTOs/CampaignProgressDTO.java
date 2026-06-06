package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignProgressDTO {
    private Long id;
    private String name;
    private String topic;
    private int totalPosts;
    private int publishedPosts;
    private String status;
}