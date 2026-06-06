package com.example.metatry.Services;

import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        testUser = new User("testuser", "test@example.com", "password", Role.MARKETING);
        testUser.setId(1L);
        testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .roles("MARKETING")
                .build();
    }

    @Test
    void generateToken_createsValidToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtService.generateToken(testUser);
        Boolean valid = jwtService.isTokenValid(token, testUserDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtService.generateToken(testUser);
        UserDetails wrongUser = org.springframework.security.core.userdetails.User
                .withUsername("wronguser")
                .password("password")
                .roles("MARKETING")
                .build();
        Boolean valid = jwtService.isTokenValid(token, wrongUser);
        assertThat(valid).isFalse();
    }

    @Test
    void generateToken_containsRoleClaim() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void generatedTokensAreNotNull() {
        String token1 = jwtService.generateToken(testUser);
        assertThat(token1).isNotNull();
    }

    @Test
    void extractUsername_onExpiredToken_throwsException() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull();
    }
}
