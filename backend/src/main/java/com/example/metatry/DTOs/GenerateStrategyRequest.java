package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateStrategyRequest {

    private String topic;
    private Integer durationWeeks;
    private Boolean autoGenerate;
}
