package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void register_createsUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("newuser");
        request.setEmail("new@example.com");
        request.setPassword("pass123");

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void register_whenNameExists_throws() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("existing");
        request.setEmail("new@example.com");
        request.setPassword("pass123");

        when(userRepository.findByName("existing")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenEmailExists_throws() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("newuser");
        request.setEmail("exists@example.com");
        request.setPassword("pass123");

        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returnsToken() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("pass123");

        User user = new User();
        user.setName("testuser");
        user.setRole(Role.ADMIN);
        user.setBanned(false);

        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.name").value("testuser"));
    }

    @Test
    void login_whenBanned_throws() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("banned");
        request.setPassword("pass123");

        User user = new User();
        user.setName("banned");
        user.setBanned(true);

        when(userRepository.findByName("banned")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
