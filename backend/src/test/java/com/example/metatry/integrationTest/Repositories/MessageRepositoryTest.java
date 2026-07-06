package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.MessageRole;
import com.example.metatry.Models.Conversation;
import com.example.metatry.Models.Message;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.MessageRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MessageRepositoryTest {

    @Autowired private MessageRepository messageRepository;
    @Autowired private ConversationRepository conversationRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        Conversation conv = conversationRepository.save(
                Conversation.builder().title("Chat").conclusion("Summary").build());
        messageRepository.save(Message.builder()
                .role(MessageRole.USER).content("Hello").conversation(conv).build());
    }

    @Test
    void findByConversationIdOrderByTimestampAsc() {
        Long convId = conversationRepository.findAll().get(0).getId();
        assertThat(messageRepository.findByConversationIdOrderByTimestampAsc(convId)).isNotEmpty();
    }

    @Test
    void findTop2ByRoleOrderByTimestampDesc() {
        assertThat(messageRepository.findTop2ByRoleOrderByTimestampDesc(MessageRole.USER)).isNotEmpty();
    }
}
