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
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalCommentId; // ✅ IMPORTANT (Facebook comment ID)

    private String commentText;

    private String sentiment; // POSITIVE / NEGATIVE / NEUTRAL

    private String authorName;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}