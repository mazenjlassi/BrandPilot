package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraped_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "platform")
    private String platform;

    @Column(name = "post_text", columnDefinition = "TEXT")
    private String postText;

    @Column(name = "post_url", length = 500)
    private String postUrl;

    @Column(name = "posted_at")
    private String postedAt;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;

    @Column(name = "topic")
    private String topic;

    @Column(name = "used_for_pattern")
    private Boolean usedForPattern;

    @PrePersist
    protected void onCreate() {
        scrapedAt = LocalDateTime.now();
        if (usedForPattern == null) usedForPattern = false;
    }
}