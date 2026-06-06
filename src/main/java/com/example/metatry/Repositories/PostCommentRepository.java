package com.example.metatry.Repositories;

import com.example.metatry.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostId(Long postId);

    List<PostComment> findByPostCampaignId(Long campaignId);

    List<PostComment> findByPostIdAndSentiment(Long postId, String sentiment);

    long countByPostId(Long postId); // 🔥 FIX

    boolean existsByExternalCommentId(String externalCommentId);

    @Query("SELECT c FROM PostComment c WHERE c.post.platform = :platform AND c.createdAt >= :since")
    List<PostComment> findByPlatformAndCreatedAtAfter(
        @Param("platform") com.example.metatry.Enums.PlatformType platform,
        @Param("since") LocalDateTime since
    );
}