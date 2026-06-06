package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer likes;

    private Integer comments;

    private Integer shares;

    private Integer impressions;

    private LocalDateTime collectedAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}