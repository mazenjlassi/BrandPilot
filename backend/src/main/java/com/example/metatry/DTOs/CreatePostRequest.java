package com.example.metatry.DTOs;

import com.example.metatry.Enums.PlatformType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreatePostRequest {

    private String title;
    private String content;
    private String hashtags;

    private PlatformType platform;

    private String imageUrl;

    private LocalDateTime scheduledAt;

    private boolean permanent; // ✅ keep your business logic

    private String link;
}