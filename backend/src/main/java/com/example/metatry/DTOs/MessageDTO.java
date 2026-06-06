package com.example.metatry.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageDTO {

    private Long id;
    private String role; // USER / AI
    private String content;
    private LocalDateTime timestamp;
}