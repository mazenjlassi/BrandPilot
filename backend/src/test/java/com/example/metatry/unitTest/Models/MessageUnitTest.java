package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import com.example.metatry.Enums.MessageRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MessageUnitTest {

    @Test
    void builder_setsAllFields() {
        Conversation conversation = Conversation.builder().id(1L).build();
        LocalDateTime now = LocalDateTime.now();

        Message message = Message.builder()
                .id(1L)
                .role(MessageRole.USER)
                .content("Hello, what is AI?")
                .timestamp(now)
                .conversation(conversation)
                .build();

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getRole()).isEqualTo(MessageRole.USER);
        assertThat(message.getContent()).isEqualTo("Hello, what is AI?");
        assertThat(message.getTimestamp()).isEqualTo(now);
        assertThat(message.getConversation()).isSameAs(conversation);
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        Message message = new Message();
        assertThat(message.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Conversation conversation = Conversation.builder().id(2L).build();
        Message message = new Message(1L, MessageRole.AI, "Response text", null, conversation);

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getRole()).isEqualTo(MessageRole.AI);
        assertThat(message.getContent()).isEqualTo("Response text");
        assertThat(message.getConversation()).isSameAs(conversation);
    }

    @Test
    void setters_updateFields() {
        Message message = new Message();

        message.setRole(MessageRole.USER);
        message.setContent("New content");
        message.setTimestamp(LocalDateTime.now());

        assertThat(message.getRole()).isEqualTo(MessageRole.USER);
        assertThat(message.getContent()).isEqualTo("New content");
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    void relationship_conversation() {
        Conversation conversation = Conversation.builder().id(10L).title("Test Chat").build();
        Message message = Message.builder().conversation(conversation).build();

        assertThat(message.getConversation()).isSameAs(conversation);
        assertThat(message.getConversation().getId()).isEqualTo(10L);
        assertThat(message.getConversation().getTitle()).isEqualTo("Test Chat");
    }

    @Test
    void nullFields_areHandled() {
        Message message = Message.builder().build();

        assertThat(message.getId()).isNull();
        assertThat(message.getRole()).isNull();
        assertThat(message.getContent()).isNull();
        assertThat(message.getTimestamp()).isNull();
        assertThat(message.getConversation()).isNull();
    }

    @Test
    void role_enumValues() {
        Message userMsg = Message.builder().role(MessageRole.USER).build();
        Message aiMsg = Message.builder().role(MessageRole.AI).build();

        assertThat(userMsg.getRole()).isEqualTo(MessageRole.USER);
        assertThat(aiMsg.getRole()).isEqualTo(MessageRole.AI);
    }

    @Test
    void content_longText() {
        String longText = "A".repeat(5000);
        Message message = Message.builder().content(longText).build();

        assertThat(message.getContent()).hasSize(5000);
    }
}
