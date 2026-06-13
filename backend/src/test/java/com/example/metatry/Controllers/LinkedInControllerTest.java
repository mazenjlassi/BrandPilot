package com.example.metatry.Controllers;

import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.LinkedInService;
import com.example.metatry.Services.LinkedInTokenService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(LinkedInController.class)
class LinkedInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LinkedInService linkedInService;

    @MockitoBean
    private LinkedInTokenService tokenService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAuthUrl_returnsUrl() throws Exception {
        when(tokenService.getAuthorizationUrl()).thenReturn("https://linkedin.com/auth");

        mockMvc.perform(get("/api/linkedin/auth-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUrl").value("https://linkedin.com/auth"));
    }

    @Test
    void callback_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/linkedin/callback")
                        .param("code", "authcode123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void callback_tokenExchangeFails_returnsUnauthorized() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Invalid code"))
                .when(tokenService).exchangeAuthorizationCode("badcode");

        mockMvc.perform(get("/api/linkedin/callback")
                        .param("code", "badcode"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void postText_returnsOk() throws Exception {
        when(linkedInService.postText("Hello")).thenReturn(Map.of("success", true, "postId", "123"));

        mockMvc.perform(post("/api/linkedin/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void postText_emptyText_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/linkedin/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le texte est requis"));
    }

    @Test
    @WithMockUser
    void postText_serviceFails_returnsBadRequest() throws Exception {
        when(linkedInService.postText("Hello"))
                .thenReturn(Map.of("success", false, "error", "API error"));

        mockMvc.perform(post("/api/linkedin/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Hello"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void postImage_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/linkedin/post/image")
                        .file(file)
                        .param("text", "Caption"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier est vide"));
    }

    @Test
    @WithMockUser
    void postImage_wrongType_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/linkedin/post/image")
                        .file(file)
                        .param("text", "Caption"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier doit être une image"));
    }

    @Test
    @WithMockUser
    void postImage_cloudinaryFails_returnsBadRequest() throws Exception {
        when(cloudinaryService.uploadWithOptions(any(), any()))
                .thenThrow(new RuntimeException("Upload failed"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/api/linkedin/post/image")
                        .file(file)
                        .param("text", "Caption"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void postImageFromUrl_returnsOk() throws Exception {
        when(linkedInService.postArticleWithImage("Text", "https://img.url", "Image"))
                .thenReturn(Map.of("success", true, "postId", "123"));

        mockMvc.perform(post("/api/linkedin/post/image/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("imageUrl", "https://img.url", "text", "Text"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void postImageFromUrl_missingFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/linkedin/post/image/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Text"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("imageUrl et text sont requis"));
    }

    @Test
    @WithMockUser
    void postVideo_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.mp4", "video/mp4", new byte[0]);

        mockMvc.perform(multipart("/api/linkedin/post/video")
                        .file(file)
                        .param("text", "Text"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier est vide"));
    }

    @Test
    @WithMockUser
    void postVideo_wrongType_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/linkedin/post/video")
                        .file(file)
                        .param("text", "Text"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le fichier doit être une vidéo"));
    }

    @Test
    @WithMockUser
    void postVideoFromUrl_returnsOk() throws Exception {
        when(linkedInService.postArticleWithVideo(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true, "postId", "123"));

        mockMvc.perform(post("/api/linkedin/post/video/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("videoUrl", "https://vid.url", "text", "Text"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void postVideoFromUrl_missingFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/linkedin/post/video/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Text"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("videoUrl et text sont requis"));
    }

    @Test
    @WithMockUser
    void getProfile_returnsProfile() throws Exception {
        when(linkedInService.getUserProfile()).thenReturn(Map.of("name", "John"));

        mockMvc.perform(get("/api/linkedin/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getStatus_returnsStatus() throws Exception {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        mockMvc.perform(get("/api/linkedin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.personUrn").value("urn:li:person:123"));
    }

    @Test
    @WithMockUser
    void postImageFromUrl_linkedInFails_returnsBadRequest() throws Exception {
        when(linkedInService.postArticleWithImage("Text", "https://img.url", "Image"))
                .thenReturn(Map.of("success", false, "error", "API error"));

        mockMvc.perform(post("/api/linkedin/post/image/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("imageUrl", "https://img.url", "text", "Text"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void postVideoFromUrl_linkedInFails_returnsBadRequest() throws Exception {
        when(linkedInService.postArticleWithVideo("Text", "https://vid.url", "Video"))
                .thenReturn(Map.of("success", false, "error", "API error"));

        mockMvc.perform(post("/api/linkedin/post/video/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("videoUrl", "https://vid.url", "text", "Text"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void postText_linkedInFails_returnsBadRequest() throws Exception {
        when(linkedInService.postText("Hello"))
                .thenReturn(Map.of("success", false, "error", "API error"));

        mockMvc.perform(post("/api/linkedin/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Hello"))))
                .andExpect(status().isBadRequest());
    }
}
