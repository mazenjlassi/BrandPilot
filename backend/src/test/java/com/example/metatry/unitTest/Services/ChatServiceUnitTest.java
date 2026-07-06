package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.ConversationDTO;
import com.example.metatry.DTOs.MessageDTO;
import com.example.metatry.Enums.MessageRole;
import com.example.metatry.Models.Conversation;
import com.example.metatry.Models.Message;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceUnitTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private GeminiService geminiService;

    @Mock
    private MemoryContextService memoryContextService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(conversationRepository, messageRepository, geminiService, memoryContextService);
    }

    @Test
    void createConversation_withTitle_createsAndReturns() {
        when(conversationRepository.save(any())).thenAnswer(i -> {
            Conversation c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        ConversationDTO result = chatService.createConversation("My Chat");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("My Chat");
    }

    @Test
    void createConversation_withBlankTitle_usesDefault() {
        when(conversationRepository.save(any())).thenAnswer(i -> {
            Conversation c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        ConversationDTO result = chatService.createConversation("");

        assertThat(result.getTitle()).isEqualTo("New Chat");
    }

    @Test
    void getAllConversations_returnsAll() {
        when(conversationRepository.findAll()).thenReturn(List.of(
                Conversation.builder().id(1L).title("Chat 1").build(),
                Conversation.builder().id(2L).title("Chat 2").build()
        ));

        List<ConversationDTO> result = chatService.getAllConversations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Chat 1");
    }

    @Test
    void getConversation_whenFound_returns() {
        Conversation conversation = Conversation.builder().id(1L).title("Chat").build();
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        ConversationDTO result = chatService.getConversation(1L);

        assertThat(result.getTitle()).isEqualTo("Chat");
    }

    @Test
    void getConversation_whenNotFound_throwsException() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> chatService.getConversation(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conversation not found");
    }

    @Test
    void deleteConversation_deletes() {
        chatService.deleteConversation(1L);
        verify(conversationRepository).deleteById(1L);
    }

    @Test
    void getMessages_returnsMappedMessages() {
        Message msg = Message.builder()
                .id(1L).role(MessageRole.USER).content("Hello").timestamp(LocalDateTime.now())
                .build();
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(List.of(msg));

        List<MessageDTO> result = chatService.getMessages(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("USER");
        assertThat(result.get(0).getContent()).isEqualTo("Hello");
    }

    @Test
    void sendMessage_savesUserAndAiMessages() {
        Conversation conversation = Conversation.builder().id(1L).title("New Chat").build();
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(m.getRole() == MessageRole.USER ? 1L : 2L);
            return m;
        });
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(List.of(
                Message.builder().role(MessageRole.USER).content("Hello").timestamp(LocalDateTime.now()).build()
        ));
        when(memoryContextService.getRecentContext()).thenReturn("Context");
        when(geminiService.generate(anyString())).thenReturn("AI response");

        MessageDTO result = chatService.sendMessage(1L, "Hello");

        assertThat(result.getRole()).isEqualTo("AI");
        assertThat(result.getContent()).isEqualTo("AI response");
    }

    @Test
    void generateConclusion_generatesAndSaves() {
        Conversation conversation = Conversation.builder().id(1L).build();
        Message msg1 = Message.builder().content("What's the best approach?").build();
        Message msg2 = Message.builder().content("Use automation tools").build();

        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(List.of(msg1, msg2));
        when(memoryContextService.getRecentContext()).thenReturn("Context");
        when(geminiService.generate(anyString())).thenReturn("Focus on automation");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        String result = chatService.generateConclusion(1L);

        assertThat(result).isEqualTo("Focus on automation");
        assertThat(conversation.getConclusion()).isEqualTo("Focus on automation");
    }

    @Test
    void sendMessage_whenConversationNotFound_throwsException() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> chatService.sendMessage(99L, "Hello"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conversation not found");
    }

    @Test
    void sendMessage_forFirstMessage_autoGeneratesTitle() {
        Conversation conversation = Conversation.builder().id(1L).title("new chat").build();
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(m.getRole() == MessageRole.USER ? 1L : 2L);
            return m;
        });
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(List.of());
        when(memoryContextService.getRecentContext()).thenReturn("Context");
        when(geminiService.generate(anyString())).thenReturn("AI Marketing Strategy");

        chatService.sendMessage(1L, "Tell me about AI marketing");

        assertThat(conversation.getTitle()).isEqualTo("AI Marketing Strategy");
    }

    @Test
    void sendMessage_whenTitleGenerationFails_keepsOriginalTitle() {
        Conversation conversation = Conversation.builder().id(1L).title("new chat").build();
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(m.getRole() == MessageRole.USER ? 1L : 2L);
            return m;
        });
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(List.of());
        when(memoryContextService.getRecentContext()).thenReturn("Context");
        when(geminiService.generate(anyString()))
                .thenThrow(new RuntimeException("API error"))
                .thenReturn("Fallback AI response");

        chatService.sendMessage(1L, "Hello");

        assertThat(conversation.getTitle()).isEqualTo("new chat");
    }
}
