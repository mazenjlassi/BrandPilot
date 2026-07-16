package com.example.metatry.Models;

import com.example.metatry.Enums.MarketingStrategyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String aiReasoning;

    private Integer durationWeeks;

    private LocalDate startDate;

    private LocalDate expectedEndDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    @Builder.Default
    private Boolean createdByAI = true;

    @Builder.Default
    private Boolean approved = false;

    @Column(columnDefinition = "TEXT")
    private String managerNotes;

    private LocalDate lastWeeklyGeneration;

    @Enumerated(EnumType.STRING)
    private MarketingStrategyStatus status;

    @Builder.Default
    private Boolean autoGenerate = false;

    @Column(columnDefinition = "TEXT")
    private String campaignPlans;

    @OneToMany(mappedBy = "marketingStrategy")
    @Builder.Default
    private List<Campaign> campaigns = new ArrayList<>();
}
