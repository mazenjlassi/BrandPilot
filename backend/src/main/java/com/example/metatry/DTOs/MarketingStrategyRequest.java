package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingStrategyRequest {

    private String title;
    private String summary;
    private String description;
    private Integer durationWeeks;
    private String managerNotes;
    private Boolean autoGenerate;
}
