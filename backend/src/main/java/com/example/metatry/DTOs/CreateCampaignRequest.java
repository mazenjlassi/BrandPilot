package com.example.metatry.DTOs;

import lombok.Data;

@Data
public class CreateCampaignRequest {

    private Long conversationId;
    private String name;
    private String topic;
}