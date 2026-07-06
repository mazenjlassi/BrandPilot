package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Models.Conversation;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ConversationRepositoryTest {

    @Autowired private ConversationRepository conversationRepository;

    @BeforeEach
    void setUp() {
        conversationRepository.deleteAll();
        conversationRepository.save(Conversation.builder().title("Chat").conclusion("Summary").build());
    }

    @Test
    void findTop2ByConclusionIsNotNull() {
        List<Conversation> convs = conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc();
        assertThat(convs).isNotEmpty();
        assertThat(convs.get(0).getConclusion()).isNotNull();
    }
}
