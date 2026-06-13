package com.example.metatry.Controllers;

import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.XService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(XController.class)
class XControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private XService xService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void postText_returnsOk() throws Exception {
        when(xService.postText("Hello world"))
                .thenReturn(Map.of("success", true, "id", "12345"));

        mockMvc.perform(post("/api/x/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Hello world"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void postText_emptyText_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/x/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void postText_tooLong_returnsBadRequest() throws Exception {
        String longText = "a".repeat(281);

        mockMvc.perform(post("/api/x/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", longText))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void postText_serviceError_returnsBadRequest() throws Exception {
        when(xService.postText("Hello")).thenReturn(Map.of("success", false, "error", "API error"));

        mockMvc.perform(post("/api/x/post/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("text", "Hello"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void test_returnsOk() throws Exception {
        mockMvc.perform(get("/api/x/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void getLimits_returnsOk() throws Exception {
        mockMvc.perform(get("/api/x/limits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void getDocs_returnsOk() throws Exception {
        mockMvc.perform(get("/api/x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("X/Twitter API"));
    }

    @Test
    void health_returnsOk() throws Exception {
        mockMvc.perform(get("/api/x/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
