package com.example.metatry.Controllers;

import com.example.metatry.Services.InstagramService;
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

@WebMvcTest(InstagramPostController.class)
class InstagramPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InstagramService instagramService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void postUrl_returnsOk() throws Exception {
        when(instagramService.postPhotoFromUrl("https://img.url", "Caption"))
                .thenReturn(Map.of("id", "12345"));

        mockMvc.perform(post("/api/instagram/post/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("imageUrl", "https://img.url", "caption", "Caption"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postUrl_missingUrl_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/instagram/post/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("caption", "Caption"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Image URL required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postLocal_returnsOk() throws Exception {
        when(instagramService.postLocalPhoto(any(), any()))
                .thenReturn(Map.of("id", "12345"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/api/instagram/post/local")
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

        mockMvc.perform(multipart("/api/instagram/post/local")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is empty"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postLocal_wrongType_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());

        mockMvc.perform(multipart("/api/instagram/post/local")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File must be image"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void test_returnsOk() throws Exception {
        mockMvc.perform(get("/api/instagram/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }
}
