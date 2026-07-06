package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.DTOs.ConversationDTO;
import com.example.metatry.DTOs.CreateConversationRequest;
import com.example.metatry.DTOs.CreateMessageRequest;
import com.example.metatry.DTOs.MessageDTO;
import com.example.metatry.Services.ChatService;
import com.example.metatry.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAll_returnsConversations() throws Exception {
        when(chatService.getAllConversations()).thenReturn(List.of(ConversationDTO.builder().build()));

        mockMvc.perform(get("/chat/conversations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getById_returnsConversation() throws Exception {
        when(chatService.getConversation(1L)).thenReturn(ConversationDTO.builder().build());

        mockMvc.perform(get("/chat/conversations/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void create_returnsConversation() throws Exception {
        CreateConversationRequest request = new CreateConversationRequest();
        request.setTitle("New Chat");

        ConversationDTO dto = ConversationDTO.builder().build();
        when(chatService.createConversation("New Chat")).thenReturn(dto);

        mockMvc.perform(post("/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void delete_conversation() throws Exception {
        doNothing().when(chatService).deleteConversation(1L);

        mockMvc.perform(delete("/chat/conversations/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMessages_returnsMessages() throws Exception {
        when(chatService.getMessages(1L)).thenReturn(List.of(MessageDTO.builder().build()));

        mockMvc.perform(get("/chat/conversations/1/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void sendMessage_returnsMessage() throws Exception {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("Hello");

        MessageDTO dto = MessageDTO.builder().build();
        when(chatService.sendMessage(1L, "Hello")).thenReturn(dto);

        mockMvc.perform(post("/chat/conversations/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void generateConclusion_returnsString() throws Exception {
        when(chatService.generateConclusion(1L)).thenReturn("Conclusion");

        mockMvc.perform(post("/chat/conversations/1/conclusion"))
                .andExpect(status().isOk())
                .andExpect(content().string("Conclusion"));
    }

}
