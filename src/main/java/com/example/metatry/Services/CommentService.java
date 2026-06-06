package com.example.metatry.Services;

import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostCommentRepository commentRepository;

    // ================= GET BY POST =================
    public List<PostComment> getCommentsByPost(Long postId){
        return commentRepository.findByPostId(postId);
    }

    // ================= GET BY CAMPAIGN =================
    public List<PostComment> getCommentsByCampaign(Long campaignId){
        return commentRepository.findByPostCampaignId(campaignId);
    }

    // ================= GET BY POST + SENTIMENT =================
    public List<PostComment> getCommentsByPostAndSentiment(Long postId, String sentiment){
        return commentRepository.findByPostIdAndSentiment(postId, sentiment);
    }

    // ================= SAVE =================
    public PostComment save(PostComment comment){
        return commentRepository.save(comment);
    }
}