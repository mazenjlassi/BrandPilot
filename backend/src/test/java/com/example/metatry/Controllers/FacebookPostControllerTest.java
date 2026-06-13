package com.example.metatry.Controllers;

import com.example.metatry.Services.FacebookService;
import com.example.metatry.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacebookPostController.class)
class FacebookPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private FacebookService facebookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void postText_returnsOk() throws Exception {
        when(facebookService.postText("Hello")).thenReturn(Map.of("id", "12345"));

        mockMvc.perform(post("/api/facebook/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("message", "Hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postText_missingMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/facebook/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("message", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Message is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postPhotoUrl_returnsOk() throws Exception {
        when(facebookService.postPhotoFromUrl("https://img.url", "Caption"))
                .thenReturn(Map.of("id", "12345"));

        mockMvc.perform(post("/api/facebook/post/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("imageUrl", "https://img.url", "caption", "Caption"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postPhotoUrl_missingUrl_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/facebook/post/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("caption", "Caption"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Image URL required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postLocal_returnsOk() throws Exception {
        when(facebookService.postLocalPhoto(any(), any()))
                .thenReturn(Map.of("id", "12345"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/api/facebook/post/local")
                        .file(file)
                        .param("caption", "Caption"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postLocal_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/facebook/post/local")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is empty"));
    }
}
