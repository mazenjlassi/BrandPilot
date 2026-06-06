package com.example.metatry.Repositories;

import com.example.metatry.Models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findTop2ByConclusionIsNotNullOrderByCreatedAtDesc();
}