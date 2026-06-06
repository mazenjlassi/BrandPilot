package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDTO {

    private Long id;
    private String name;
    private String topic;
    private String platform;
    private String status;
    private int postCount;
}