package com.example.metatry.Services;

import com.example.metatry.DTOs.*;
import com.example.metatry.Enums.MessageRole;
import com.example.metatry.Models.Conversation;
import com.example.metatry.Models.Message;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GeminiService geminiService;
    private final MemoryContextService memoryContextService;

    // ================= MAPPERS =================

    private ConversationDTO mapConversation(Conversation c) {
        return ConversationDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .conclusion(c.getConclusion())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private MessageDTO mapMessage(Message m) {
        return MessageDTO.builder()
                .id(m.getId())
                .role(m.getRole().name())
                .content(m.getContent())
                .timestamp(m.getTimestamp())
                .build();
    }

    // ================= CONVERSATIONS =================

    public ConversationDTO createConversation(String title) {

        Conversation c = Conversation.builder()
                .title(title == null || title.isBlank() ? "New Chat" : title)
                .createdAt(LocalDateTime.now())
                .build();

        return mapConversation(conversationRepository.save(c));
    }

    public List<ConversationDTO> getAllConversations() {
        return conversationRepository.findAll()
                .stream()
                .map(this::mapConversation)
                .toList();
    }

    public ConversationDTO getConversation(Long id) {
        return conversationRepository.findById(id)
                .map(this::mapConversation)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public void deleteConversation(Long id) {
        conversationRepository.deleteById(id);
    }

    // ================= MESSAGES =================

    public List<MessageDTO> getMessages(Long conversationId) {
        return messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    public MessageDTO sendMessage(Long conversationId, String userMessage) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 1. Save user message
        Message user = Message.builder()
                .role(MessageRole.USER)
                .content(userMessage)
                .timestamp(LocalDateTime.now())
                .conversation(conversation)
                .build();

        messageRepository.save(user);

        // 🔥 AUTO TITLE (FIRST MESSAGE ONLY)
        if (conversation.getTitle() == null ||
                conversation.getTitle().trim().equalsIgnoreCase("new chat")) {

            try {
                String titlePrompt = """
Generate a short (3-5 words) technical title.
Must be specific and IT-related.
No generic names like "New Chat".
No quotes.

Message:
""" + userMessage;

                String memory = memoryContextService.getRecentContext();
                String fullTitlePrompt = titlePrompt + "\n\n" + memory;

                String generatedTitle = geminiService.generate(fullTitlePrompt);

                if (generatedTitle != null && !generatedTitle.isBlank()) {

                    generatedTitle = generatedTitle.trim();

                    // Limit length
                    if (generatedTitle.length() > 50) {
                        generatedTitle = generatedTitle.substring(0, 50);
                    }

                    conversation.setTitle(generatedTitle);
                    conversationRepository.save(conversation);
                }

            } catch (Exception e) {
                System.out.println("Title generation failed: " + e.getMessage());
            }
        }

        // 2. Get history
        List<Message> history = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);

        List<String> messages = history.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .toList();

        // 3. Call AI
        String prompt = """
You are an IT expert assistant focused on software, systems, and digital products.

Rules:
- Answer ONLY in IT / tech domain
- Be concise and structured
- Use bullet points when possible
- Avoid long paragraphs
- Give practical and actionable answers
-the response can't be longer than 3 lines
- If question is not IT-related, redirect it to a tech perspective

Conversation:
""" + String.join("\n", messages);

        String memory = memoryContextService.getRecentContext();
        String fullPrompt = prompt + "\n\n" + memory;

        String aiResponse = geminiService.generate(fullPrompt);

        // 4. Save AI response
        Message ai = Message.builder()
                .role(MessageRole.AI)
                .content(aiResponse)
                .timestamp(LocalDateTime.now())
                .conversation(conversation)
                .build();

        return mapMessage(messageRepository.save(ai));
    }

    // ================= CONCLUSION =================

    public String generateConclusion(Long conversationId) {

        List<Message> history = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);

        List<String> texts = history.stream()
                .map(Message::getContent)
                .toList();

        String prompt = """
Summarize this discussion into a clear actionable conclusion (2-3 sentences):

""" + String.join("\n", texts);

        String memory = memoryContextService.getRecentContext();
        String fullPrompt = prompt + "\n\n" + memory;

        String result = geminiService.generate(fullPrompt);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow();

        conversation.setConclusion(result);
        conversationRepository.save(conversation);

        return result;
    }
}