package com.example.metatry.Controllers;

import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.SocialPublisherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostPublishController.class)
class PostPublishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private SocialPublisherService socialPublisherService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void publishPost_returnsPost() throws Exception {
        Post post = new Post();
        post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(socialPublisherService.publishPost(post)).thenReturn(post);

        mockMvc.perform(post("/publish/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void publishPost_postNotFound_returnsError() throws Exception {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/publish/99"))
                .andExpect(status().is4xxClientError());
    }
}
