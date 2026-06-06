package com.example.metatry.DTOs;

import com.example.metatry.Enums.PlatformType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdatePostRequest {
    private String title;

    private String content;

    private String hashtags;

    private PlatformType platform;

    private String imageUrl;

    private String videoUrl;

    private Boolean approved;

    private LocalDateTime scheduledAt;

    private Boolean permanent;

    private String link;


}