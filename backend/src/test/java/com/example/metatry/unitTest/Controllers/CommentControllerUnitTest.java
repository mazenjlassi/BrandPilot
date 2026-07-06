package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.Models.PostComment;
import com.example.metatry.Services.CommentService;
import com.example.metatry.Services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCommentsByPost_returnsComments() throws Exception {
        when(commentService.getCommentsByPost(1L)).thenReturn(List.of(new PostComment()));

        mockMvc.perform(get("/comments/post/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCommentsByCampaign_returnsComments() throws Exception {
        when(commentService.getCommentsByCampaign(1L)).thenReturn(List.of(new PostComment()));

        mockMvc.perform(get("/comments/campaign/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCommentsByPostAndSentiment_returnsComments() throws Exception {
        when(commentService.getCommentsByPostAndSentiment(1L, "positive"))
                .thenReturn(List.of(new PostComment()));

        mockMvc.perform(get("/comments/post/1/sentiment/positive"))
                .andExpect(status().isOk());
    }


}
