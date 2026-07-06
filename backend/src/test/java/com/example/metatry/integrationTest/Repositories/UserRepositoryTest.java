package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("John", "john@test.com", "pass", Role.ADMIN));
    }

    @Test
    void findByName() {
        Optional<User> user = userRepository.findByName("John");
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void findByEmail() {
        assertThat(userRepository.findByEmail("john@test.com")).isPresent();
    }
}
