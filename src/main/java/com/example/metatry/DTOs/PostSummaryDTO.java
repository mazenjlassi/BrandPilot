package com.example.metatry.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostSummaryDTO {

    private Long id;
    private String title;
    private String platform;
    private String status;

    private Integer likes;
    private Integer commentsCount;
    private Integer shares;
}