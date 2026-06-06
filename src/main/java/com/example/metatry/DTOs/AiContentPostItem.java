package com.example.metatry.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiContentPostItem {
    private String platform;
    private String title;
    private String content;
    private List<String> hashtags;
    private String imagePrompt;
}
