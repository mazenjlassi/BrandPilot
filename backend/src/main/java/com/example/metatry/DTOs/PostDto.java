package com.example.metatry.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDto {

    private Long id;

    private String title;
    private String content;
    private String hashtags;

    private String platform;

    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private Long campaignId;
    private String campaignName;

    private String imageUrl;

    private boolean permanent;
    private String status;

    private String link;

    private Integer likes;
    private Integer commentsCount;
    private Integer shares;
}