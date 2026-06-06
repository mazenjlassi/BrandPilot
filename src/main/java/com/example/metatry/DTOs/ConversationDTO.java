package com.example.metatry.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationDTO {

    private Long id;
    private String title;
    private String conclusion;
    private LocalDateTime createdAt;
}