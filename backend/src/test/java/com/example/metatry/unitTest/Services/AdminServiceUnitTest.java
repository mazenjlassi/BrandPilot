package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.UserStatsDTO;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, passwordEncoder);
    }

    private User createUser(Long id, String name, Role role, Boolean banned) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setRole(role);
        u.setBanned(banned);
        return u;
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                createUser(1L, "u1", Role.ADMIN, false),
                createUser(2L, "u2", Role.MARKETING, false)
        ));
        assertThat(adminService.getAllUsers()).hasSize(2);
    }

    @Test
    void getUserById_whenFound_returnsUser() {
        User user = createUser(1L, "John", Role.ADMIN, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThat(adminService.getUserById(1L).getName()).isEqualTo("John");
    }

    @Test
    void getUserById_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createUser_savesAndReturnsUser() {
        when(userRepository.findByName("John")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret123");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = adminService.createUser("John", "john@test.com", "secret123", Role.MARKETING);

        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-secret123");
        assertThat(result.getRole()).isEqualTo(Role.MARKETING);
        verify(userRepository).save(any());
    }

    @Test
    void createUser_whenUsernameExists_throwsException() {
        when(userRepository.findByName("John")).thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> adminService.createUser("John", "email", "pass", Role.MARKETING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_whenEmailExists_throwsException() {
        when(userRepository.findByName("John")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> adminService.createUser("John", "john@test.com", "pass", Role.MARKETING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void deleteUser_whenNotAdmin_deletes() {
        User user = createUser(1L, "u", Role.MARKETING, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_whenAdmin_throwsException() {
        User user = createUser(1L, "u", Role.ADMIN, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete admin user");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateUserRole_changesAndSaves() {
        User user = createUser(1L, "u", Role.MARKETING, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = adminService.updateUserRole(1L, Role.ADMIN);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void banUser_whenNotAdmin_bans() {
        User user = createUser(1L, "u", Role.MARKETING, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = adminService.banUser(1L);

        assertThat(result.getBanned()).isTrue();
    }

    @Test
    void banUser_whenAdmin_throwsException() {
        User user = createUser(1L, "u", Role.ADMIN, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.banUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot ban admin user");
    }

    @Test
    void unbanUser_setsBannedFalse() {
        User user = createUser(1L, "u", Role.MARKETING, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = adminService.unbanUser(1L);

        assertThat(result.getBanned()).isFalse();
    }

    @Test
    void getUserStats_returnsCorrectCounts() {
        when(userRepository.findAll()).thenReturn(List.of(
                createUser(1L, "a", Role.ADMIN, false),
                createUser(2L, "m1", Role.MARKETING, false),
                createUser(3L, "m2", Role.MARKETING, true),
                createUser(4L, "m3", Role.MARKETING, false)
        ));

        UserStatsDTO stats = adminService.getUserStats();

        assertThat(stats.getTotalUsers()).isEqualTo(4);
        assertThat(stats.getTotalMarketing()).isEqualTo(3);
        assertThat(stats.getActiveUsers()).isEqualTo(3);
        assertThat(stats.getBannedUsers()).isEqualTo(1);
    }

    @Test
    void getUserStats_whenNoUsers_returnsZero() {
        when(userRepository.findAll()).thenReturn(List.of());

        UserStatsDTO stats = adminService.getUserStats();

        assertThat(stats.getTotalUsers()).isZero();
        assertThat(stats.getTotalMarketing()).isZero();
        assertThat(stats.getActiveUsers()).isZero();
        assertThat(stats.getBannedUsers()).isZero();
    }
}
