package com.example.metatry.Controllers;

import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyProfileController.class)
class CompanyProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CompanyProfileRepository repository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAll_returnsProfiles() throws Exception {
        when(repository.findAll()).thenReturn(List.of(new CompanyProfile()));

        mockMvc.perform(get("/api/company-profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getByCompanyName_returnsProfile() throws Exception {
        CompanyProfile profile = new CompanyProfile();
        profile.setCompanyName("MetaTry");
        when(repository.findByCompanyName("MetaTry")).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/company-profiles/by-name/MetaTry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("MetaTry"));
    }

    @Test
    void getByCompanyName_notFound_returns404() throws Exception {
        when(repository.findByCompanyName("Unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/company-profiles/by-name/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsProfile() throws Exception {
        CompanyProfile profile = new CompanyProfile();
        profile.setCompanyName("NewCo");
        when(repository.existsByCompanyName("NewCo")).thenReturn(false);
        when(repository.save(any())).thenReturn(profile);

        mockMvc.perform(post("/api/company-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("companyName", "NewCo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("NewCo"));
    }

    @Test
    void create_missingCompanyName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/company-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("companyName", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicateCompany_returnsBadRequest() throws Exception {
        when(repository.existsByCompanyName("Existing")).thenReturn(true);

        mockMvc.perform(post("/api/company-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("companyName", "Existing"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returnsUpdated() throws Exception {
        CompanyProfile existing = new CompanyProfile();
        existing.setCompanyName("Old");
        CompanyProfile updated = new CompanyProfile();
        updated.setCompanyName("New");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/company-profiles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("companyName", "New"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("New"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/company-profiles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("companyName", "New"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsOk() throws Exception {
        when(repository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/company-profiles/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        when(repository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/company-profiles/99"))
                .andExpect(status().isNotFound());
    }
}
