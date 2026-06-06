package com.example.metatry.DTOs;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private PostStatus status;
    private PlatformType platform;
    private String imageUrl;
    private Long campaignId;
    private String campaignName;
}