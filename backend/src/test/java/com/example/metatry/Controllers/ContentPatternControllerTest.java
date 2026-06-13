package com.example.metatry.Controllers;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.ContentPatternService;
import com.example.metatry.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContentPatternController.class)
class ContentPatternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ContentPatternService contentPatternService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAll_returnsPatterns() throws Exception {
        when(contentPatternService.getAll()).thenReturn(List.of(new ContentPattern()));

        mockMvc.perform(get("/api/patterns/crud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_returnsPattern() throws Exception {
        ContentPattern pattern = ContentPattern.builder().id(1L).topic("AI").build();
        when(contentPatternService.getById(1L)).thenReturn(pattern);

        mockMvc.perform(get("/api/patterns/crud/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("AI"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(contentPatternService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/patterns/crud/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsPattern() throws Exception {
        ContentPattern pattern = ContentPattern.builder().id(1L).topic("AI").build();
        when(contentPatternService.save(any())).thenReturn(pattern);

        mockMvc.perform(post("/api/patterns/crud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("topic", "AI"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("AI"));
    }

    @Test
    void update_returnsUpdated() throws Exception {
        ContentPattern updated = ContentPattern.builder().id(1L).topic("ML").build();
        when(contentPatternService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/patterns/crud/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("topic", "ML"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("ML"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(contentPatternService.update(eq(99L), any())).thenReturn(null);

        mockMvc.perform(put("/api/patterns/crud/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("topic", "ML"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/patterns/crud/1"))
                .andExpect(status().isOk());
    }

    @Test
    void exists_returnsTrue() throws Exception {
        when(contentPatternService.exists("AI")).thenReturn(true);

        mockMvc.perform(get("/api/patterns/crud/exists")
                        .param("topic", "AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
