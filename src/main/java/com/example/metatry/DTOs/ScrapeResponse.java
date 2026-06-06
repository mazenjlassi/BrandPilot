package com.example.metatry.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapeResponse {
    private String companyName;
    private int totalPosts;
    private Map<String, List<ScrapedPostDTO>> results;
    private String status;
    private String message;
}