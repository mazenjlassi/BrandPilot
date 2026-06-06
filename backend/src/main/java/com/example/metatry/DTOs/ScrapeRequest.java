package com.example.metatry.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapeRequest {
    private String companyName;
    private String linkedin;
    private String instagram;
    private String facebook;
}