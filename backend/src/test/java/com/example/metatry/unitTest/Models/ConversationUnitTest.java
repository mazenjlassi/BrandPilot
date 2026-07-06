package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import com.example.metatry.Enums.MessageRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationUnitTest {

    @Test
    void builder_setsAllFields() {
        Conversation conversation = Conversation.builder()
                .id(1L)
                .title("AI Discussion")
                .conclusion("The user learned about AI")
                .build();

        assertThat(conversation.getId()).isEqualTo(1L);
        assertThat(conversation.getTitle()).isEqualTo("AI Discussion");
        assertThat(conversation.getConclusion()).isEqualTo("The user learned about AI");
    }

    @Test
    void noArgsConstructor_initializesMessagesList() {
        Conversation conversation = new Conversation();
        assertThat(conversation.getMessages()).isNotNull();
        assertThat(conversation.getMessages()).isEmpty();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Conversation conversation = new Conversation(1L, "Title", "Conclusion", null, List.of());

        assertThat(conversation.getId()).isEqualTo(1L);
        assertThat(conversation.getTitle()).isEqualTo("Title");
        assertThat(conversation.getConclusion()).isEqualTo("Conclusion");
    }

    @Test
    void setters_updateFields() {
        Conversation conversation = new Conversation();

        conversation.setId(5L);
        conversation.setTitle("Updated Title");
        conversation.setConclusion("Updated conclusion");
        conversation.setCreatedAt(LocalDateTime.now());

        assertThat(conversation.getId()).isEqualTo(5L);
        assertThat(conversation.getTitle()).isEqualTo("Updated Title");
        assertThat(conversation.getConclusion()).isEqualTo("Updated conclusion");
        assertThat(conversation.getCreatedAt()).isNotNull();
    }

    @Test
    void relationship_messages() {
        Conversation conversation = Conversation.builder().title("Chat").build();
        Message msg1 = Message.builder().content("Hi").role(MessageRole.USER).build();
        Message msg2 = Message.builder().content("Hello").role(MessageRole.AI).build();

        conversation.setMessages(List.of(msg1, msg2));

        assertThat(conversation.getMessages()).hasSize(2);
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("Hi");
        assertThat(conversation.getMessages().get(1).getContent()).isEqualTo("Hello");
    }

    @Test
    void nullFields_areHandled() {
        Conversation conversation = Conversation.builder().build();

        assertThat(conversation.getId()).isNull();
        assertThat(conversation.getTitle()).isNull();
        assertThat(conversation.getConclusion()).isNull();
        assertThat(conversation.getCreatedAt()).isNull();
        assertThat(conversation.getMessages()).isNull();
    }

    @Test
    void messagesList_isMutable() {
        Conversation conversation = new Conversation();
        Message msg = Message.builder().content("Test").build();

        conversation.getMessages().add(msg);

        assertThat(conversation.getMessages()).hasSize(1);
    }

    @Test
    void titleAndConclusion_areIndependent() {
        Conversation conversation = Conversation.builder()
                .title("Q&A")
                .conclusion("Summary of Q&A session")
                .build();

        assertThat(conversation.getTitle()).doesNotContain("Summary");
        assertThat(conversation.getConclusion()).contains("Summary");
    }

    @Test
    void createdAt_remainsNull_whenNotSet() {
        Conversation conversation = new Conversation();
        assertThat(conversation.getCreatedAt()).isNull();
    }
}
