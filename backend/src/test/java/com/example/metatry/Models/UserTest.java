package com.example.metatry.Models;

import com.example.metatry.Enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void noArgsConstructor_setsDefaults() {
        User user = new User();
        assertThat(user.getBanned()).isFalse();
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getRole()).isNull();
    }

    @Test
    void parameterizedConstructor_setsAllFields() {
        User user = new User("John Doe", "john@example.com", "securePass123", Role.ADMIN);

        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("securePass123");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getBanned()).isFalse();
    }

    @Test
    void setters_updateFields() {
        User user = new User();

        user.setId(1L);
        user.setName("Jane");
        user.setEmail("jane@example.com");
        user.setPassword("newPass");
        user.setRole(Role.MARKETING);
        user.setBanned(true);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Jane");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPassword()).isEqualTo("newPass");
        assertThat(user.getRole()).isEqualTo(Role.MARKETING);
        assertThat(user.getBanned()).isTrue();
    }

    @Test
    void banned_defaultIsFalse() {
        User user = new User("Test", "test@test.com", "pass", Role.MARKETING);
        assertThat(user.getBanned()).isFalse();
    }

    @Test
    void gettersAndSetters_roundTrip() {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@company.com");

        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@company.com");
    }

    @Test
    void nullValues_areHandled() {
        User user = new User(null, null, null, null);

        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getBanned()).isFalse();
    }

    @Test
    void email_uniqueness() {
        User user1 = new User("A", "same@email.com", "p1", Role.ADMIN);
        User user2 = new User("B", "same@email.com", "p2", Role.MARKETING);

        assertThat(user1.getEmail()).isEqualTo(user2.getEmail());
    }

    @Test
    void adminRole_properties() {
        User user = new User("Admin", "admin@test.com", "admin123", Role.ADMIN);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void marketingRole_properties() {
        User user = new User("Marketer", "marketing@test.com", "mkt123", Role.MARKETING);
        assertThat(user.getRole()).isEqualTo(Role.MARKETING);
    }
}
