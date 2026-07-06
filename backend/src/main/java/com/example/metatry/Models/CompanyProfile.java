package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", unique = true, nullable = false)
    private String companyName;

    @Column(name = "instagram_url", nullable = false)
    private String instagramUrl;

    @Column(name = "facebook_url", nullable = false)
    private String facebookUrl;

    @Column(name = "linkedin_url", nullable = false)
    private String linkedinUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}