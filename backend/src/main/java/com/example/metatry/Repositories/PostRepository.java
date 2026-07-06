package com.example.metatry.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
    SELECT p FROM Post p
    LEFT JOIN FETCH p.images
    LEFT JOIN FETCH p.campaign
    WHERE p.id = :id
""")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    List<Post> findByApprovedTrue();

    List<Post> findByStatus(PostStatus status);

    List<Post> findByPlatform(PlatformType platform);

    long countByStatus(PostStatus status);

    long countByApprovedTrue();

    long countByPlatform(PlatformType platform);
    List<Post> findByApprovedTrueAndStatusAndScheduledAtBefore(
            PostStatus status,
            LocalDateTime time
    );

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    List<Post> findByPermanentTrue();

    //  By campaign
    List<Post> findByCampaignId(Long campaignId);

    //  Combined (VERY USEFUL)
    List<Post> findByCampaignIdAndStatus(Long campaignId, PostStatus status);

    List<Post> findTop20ByStatusOrderByPublishedAtDesc(PostStatus status);

List<Post> findByStatusAndScheduledAtBetween(PostStatus status, LocalDateTime start, LocalDateTime end);

    List<Post> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status AND p.publishedAt >= :start AND p.publishedAt < :end")
    long countByStatusAndPublishedAtBetween(
        @Param("status") PostStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Post> findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(PostStatus status, LocalDateTime after);

    List<Post> findByStatusAndApprovedTrueAndScheduledAtBefore(
            PostStatus status,
            LocalDateTime dateTime
    );

    // for displaying all scheduled posts
    List<Post> findByStatusOrderByScheduledAtAsc(PostStatus status);

    List<Post> findTop3ByOrderByCreatedAtDesc();

}