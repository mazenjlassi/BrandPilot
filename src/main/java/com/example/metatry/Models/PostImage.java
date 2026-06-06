package com.example.metatry.Models;

import com.example.metatry.Enums.ImageSize;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @Column(length = 2000)
    private String imagePrompt;

    @Enumerated(EnumType.STRING)
    private ImageSize size;

    private Boolean selected = true;

    @OneToOne
    @JoinColumn(name = "post_id", unique = true)
    @JsonBackReference
    private Post post;

}
