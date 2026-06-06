package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignProgressDTO;
import com.example.metatry.DTOs.UserStatsDTO;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Services.AdminService;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsUsers() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of(new User()));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_returnsUser() throws Exception {
        when(adminService.getUserById(1L)).thenReturn(new User());

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_createsAndReturns() throws Exception {
        User user = new User();
        user.setName("newuser");
        when(adminService.createUser("newuser", "new@test.com", "pass", Role.MARKETING))
                .thenReturn(user);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "newuser", "email", "new@test.com",
                                        "password", "pass", "role", "MARKETING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_withDefaultRole() throws Exception {
        when(adminService.createUser("u", "e@e.com", "p", Role.MARKETING))
                .thenReturn(new User());

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "u", "email", "e@e.com", "password", "p"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_returnsOk() throws Exception {
        doNothing().when(adminService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_returnsUser() throws Exception {
        User user = new User();
        user.setRole(Role.ADMIN);
        when(adminService.updateUserRole(1L, Role.ADMIN)).thenReturn(user);

        mockMvc.perform(put("/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_returnsUser() throws Exception {
        User user = new User();
        user.setBanned(true);
        when(adminService.banUser(1L)).thenReturn(user);

        mockMvc.perform(put("/admin/users/1/ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unbanUser_returnsUser() throws Exception {
        User user = new User();
        user.setBanned(false);
        when(adminService.unbanUser(1L)).thenReturn(user);

        mockMvc.perform(put("/admin/users/1/unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserStats_returnsStats() throws Exception {
        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsers(10L);
        when(adminService.getUserStats()).thenReturn(stats);

        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCampaignsProgress_returnsList() throws Exception {
        when(campaignService.getCampaignsWithProgress(3)).thenReturn(List.of(new CampaignProgressDTO()));

        mockMvc.perform(get("/admin/campaigns/progress"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCampaignsProgress_withCustomLimit() throws Exception {
        when(campaignService.getCampaignsWithProgress(5)).thenReturn(List.of());

        mockMvc.perform(get("/admin/campaigns/progress").param("limit", "5"))
                .andExpect(status().isOk());
    }

}
