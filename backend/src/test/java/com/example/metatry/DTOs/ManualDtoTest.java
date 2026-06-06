package com.example.metatry.DTOs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualDtoTest {

    @Test
    void facebookPostRequest() {
        FacebookPostRequest dto = new FacebookPostRequest();
        dto.setMessage("Hello");
        dto.setPageId("page123");
        dto.setAccessToken("EAAToken...");

        assertThat(dto.getMessage()).isEqualTo("Hello");
        assertThat(dto.getPageId()).isEqualTo("page123");
        assertThat(dto.getAccessToken()).isEqualTo("EAAToken...");
    }

    @Test
    void facebookPostRequest_defaultConstructor() {
        FacebookPostRequest dto = new FacebookPostRequest();
        assertThat(dto.getMessage()).isNull();
        assertThat(dto.getPageId()).isNull();
        assertThat(dto.getAccessToken()).isNull();
    }

    @Test
    void authRequest() {
        AuthRequest dto = new AuthRequest();
        dto.setUsername("admin");
        dto.setPassword("password123");

        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getPassword()).isEqualTo("password123");
    }

    @Test
    void authRequest_defaultConstructor() {
        AuthRequest dto = new AuthRequest();
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void registerRequest() {
        RegisterRequest dto = new RegisterRequest();
        dto.setName("John");
        dto.setEmail("john@test.com");
        dto.setPassword("securePass");

        assertThat(dto.getName()).isEqualTo("John");
        assertThat(dto.getEmail()).isEqualTo("john@test.com");
        assertThat(dto.getPassword()).isEqualTo("securePass");
    }

    @Test
    void registerRequest_defaultConstructor() {
        RegisterRequest dto = new RegisterRequest();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void updatePostRequest_defaults() {
        UpdatePostRequest dto = new UpdatePostRequest();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getApproved()).isNull();
        assertThat(dto.getPermanent()).isNull();
    }
}
