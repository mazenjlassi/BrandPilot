package com.example.metatry.Models;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String content;

    @Column(length = 1000)
    private String hashtags;

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private PlatformType platform;

    private Boolean generatedByAI;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    private boolean permanent = false;

    private String link = "https://3lm-solutions2.odoo.com/contactus";

    private Integer likes;

    private Integer commentsCount;

    private Integer shares;

    private Integer impressions;

    private Double engagementScore;

    private String platformPostId;

    private Boolean approved = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostMetric> metrics;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostComment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @Transient
    public PostImage getImage() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }

    private boolean notificationSent = false;

}