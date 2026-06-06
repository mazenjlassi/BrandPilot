package com.example.metatry.Controllers;

import com.example.metatry.Models.PostComment;
import com.example.metatry.Services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ================= BY POST =================
    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<PostComment> getCommentsByPost(@PathVariable Long postId){
        return commentService.getCommentsByPost(postId);
    }

    // ================= BY CAMPAIGN =================
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<PostComment> getCommentsByCampaign(@PathVariable Long campaignId){
        return commentService.getCommentsByCampaign(campaignId);
    }

    // 🔥 NEW → FILTER BY POST + SENTIMENT
    @GetMapping("/post/{postId}/sentiment/{sentiment}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<PostComment> getCommentsByPostAndSentiment(
            @PathVariable Long postId,
            @PathVariable String sentiment){

        return commentService.getCommentsByPostAndSentiment(postId, sentiment);
    }
}