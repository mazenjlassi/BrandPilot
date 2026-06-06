package com.example.metatry.Repositories;

import com.example.metatry.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    List<Message> findTop2ByRoleOrderByTimestampDesc(com.example.metatry.Enums.MessageRole role);
}