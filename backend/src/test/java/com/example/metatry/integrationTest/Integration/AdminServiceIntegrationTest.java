package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.UserStatsDTO;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.AdminService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.save(createUser("admin", "admin@test.com", Role.ADMIN));
        userRepository.save(createUser("user1", "user1@test.com", Role.MARKETING));
        userRepository.save(createUser("user2", "user2@test.com", Role.MARKETING));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User createUser(String name, String email, Role role) {
        User u = new User(name, email, passwordEncoder.encode("pass"), role);
        return userRepository.save(u);
    }

    @Test
    void getAllUsers_returnsAll() {
        List<User> users = adminService.getAllUsers();
        assertThat(users).hasSize(3);
    }

    @Test
    void getUserById_success() {
        User u = userRepository.findAll().get(0);
        User found = adminService.getUserById(u.getId());
        assertThat(found.getName()).isEqualTo(u.getName());
    }

    @Test
    void getUserById_notFound_throws() {
        assertThatThrownBy(() -> adminService.getUserById(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createUser_success() {
        User created = adminService.createUser("newuser", "new@test.com", "pass123", Role.MARKETING);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("newuser");
        assertThat(created.getRole()).isEqualTo(Role.MARKETING);
    }

    @Test
    void createUser_duplicateName_throws() {
        assertThatThrownBy(() -> adminService.createUser("admin", "other@test.com", "pass", Role.MARKETING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_duplicateEmail_throws() {
        assertThatThrownBy(() -> adminService.createUser("other", "admin@test.com", "pass", Role.MARKETING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void deleteUser_success() {
        User u = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MARKETING).findFirst().orElseThrow();

        adminService.deleteUser(u.getId());

        assertThat(userRepository.findById(u.getId())).isEmpty();
    }

    @Test
    void deleteUser_admin_throws() {
        User admin = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN).findFirst().orElseThrow();

        assertThatThrownBy(() -> adminService.deleteUser(admin.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete admin");
    }

    @Test
    void deleteUser_notFound_throws() {
        assertThatThrownBy(() -> adminService.deleteUser(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUserRole_success() {
        User u = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MARKETING).findFirst().orElseThrow();

        User updated = adminService.updateUserRole(u.getId(), Role.ADMIN);

        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void banUser_success() {
        User u = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MARKETING).findFirst().orElseThrow();

        User banned = adminService.banUser(u.getId());

        assertThat(banned.getBanned()).isTrue();
    }

    @Test
    void banUser_admin_throws() {
        User admin = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN).findFirst().orElseThrow();

        assertThatThrownBy(() -> adminService.banUser(admin.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot ban admin");
    }

    @Test
    void unbanUser_success() {
        User u = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MARKETING).findFirst().orElseThrow();
        u.setBanned(true);
        userRepository.save(u);

        User unbanned = adminService.unbanUser(u.getId());

        assertThat(unbanned.getBanned()).isFalse();
    }

    @Test
    void getUserStats_returnsCorrectCounts() {
        UserStatsDTO stats = adminService.getUserStats();

        assertThat(stats.getTotalUsers()).isEqualTo(3);
        assertThat(stats.getTotalMarketing()).isEqualTo(2);
        assertThat(stats.getActiveUsers()).isEqualTo(3);
        assertThat(stats.getBannedUsers()).isZero();
    }

    @Test
    void getUserStats_withBannedUser() {
        User u = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MARKETING).findFirst().orElseThrow();
        u.setBanned(true);
        userRepository.save(u);

        UserStatsDTO stats = adminService.getUserStats();

        assertThat(stats.getBannedUsers()).isEqualTo(1);
        assertThat(stats.getActiveUsers()).isEqualTo(2);
    }
}
