package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostStatsResponse {

    private long totalPosts;

    private long publishedPosts;

    private long draftPosts;

    private long approvedPosts;

    private long facebookPosts;

    private long instagramPosts;

    private long linkedinPosts;

}