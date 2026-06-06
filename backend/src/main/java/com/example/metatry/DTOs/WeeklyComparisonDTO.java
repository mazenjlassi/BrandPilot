package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyComparisonDTO {
    private int thisWeek;
    private int lastWeek;
    private double percentage;
    private boolean increased;
}