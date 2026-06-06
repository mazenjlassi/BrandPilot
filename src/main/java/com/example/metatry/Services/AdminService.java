package com.example.metatry.Services;

import com.example.metatry.DTOs.UserStatsDTO;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User createUser(String name, String email, String password, Role role) {
        if (userRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete admin user");
        }

        userRepository.delete(user);
    }

    public User updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setRole(role);
        return userRepository.save(user);
    }

    public User banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot ban admin user");
        }

        user.setBanned(true);
        return userRepository.save(user);
    }

    public User unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setBanned(false);
        return userRepository.save(user);
    }

    public UserStatsDTO getUserStats() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long totalMarketing = allUsers.stream()
            .filter(u -> u.getRole() == Role.MARKETING)
            .count();
        long activeUsers = allUsers.stream()
            .filter(u -> !Boolean.TRUE.equals(u.getBanned()))
            .count();
        long bannedUsers = allUsers.stream()
            .filter(u -> Boolean.TRUE.equals(u.getBanned()))
            .count();

        return UserStatsDTO.builder()
            .totalUsers(totalUsers)
            .totalMarketing(totalMarketing)
            .activeUsers(activeUsers)
            .bannedUsers(bannedUsers)
            .build();
    }
}