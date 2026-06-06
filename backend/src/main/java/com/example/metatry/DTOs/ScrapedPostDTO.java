package com.example.metatry.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapedPostDTO {
    private String platform;
    private String postText;
    private String postedAt;
    private String url;
}