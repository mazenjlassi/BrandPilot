package com.example.metatry.Repositories;

import com.example.metatry.Models.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // ✅ Get campaigns sorted by newest first
    List<Campaign> findAllByOrderByCreatedAtDesc();

    // ✅ Fetch campaigns with posts (avoid N+1)
    @Query("SELECT DISTINCT c FROM Campaign c LEFT JOIN FETCH c.posts")
    List<Campaign> findAllWithPosts();
}