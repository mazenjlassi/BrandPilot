package com.example.metatry.Enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsTest {

    // ================= PlatformType =================

    @Test
    void platformType_hasExpectedValues() {
        assertThat(PlatformType.values()).containsExactly(
                PlatformType.FACEBOOK,
                PlatformType.INSTAGRAM,
                PlatformType.LINKEDIN
        );
    }

    @ParameterizedTest
    @CsvSource({
            "FACEBOOK, FACEBOOK",
            "INSTAGRAM, INSTAGRAM",
            "LINKEDIN, LINKEDIN"
    })
    void platformType_fromString(String input, PlatformType expected) {
        assertThat(PlatformType.valueOf(input)).isEqualTo(expected);
    }

    @Test
    void platformType_count() {
        assertThat(PlatformType.values()).hasSize(3);
    }

    // ================= PostStatus =================

    @Test
    void postStatus_hasExpectedValues() {
        assertThat(PostStatus.values()).containsExactly(
                PostStatus.DRAFT,
                PostStatus.SCHEDULED,
                PostStatus.PUBLISHED
        );
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, DRAFT",
            "SCHEDULED, SCHEDULED",
            "PUBLISHED, PUBLISHED"
    })
    void postStatus_fromString(String input, PostStatus expected) {
        assertThat(PostStatus.valueOf(input)).isEqualTo(expected);
    }

    @Test
    void postStatus_count() {
        assertThat(PostStatus.values()).hasSize(3);
    }

    // ================= Role =================

    @Test
    void role_hasExpectedValues() {
        assertThat(Role.values()).containsExactly(
                Role.ADMIN,
                Role.MARKETING
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ADMIN, ADMIN",
            "MARKETING, MARKETING"
    })
    void role_fromString(String input, Role expected) {
        assertThat(Role.valueOf(input)).isEqualTo(expected);
    }

    @Test
    void role_count() {
        assertThat(Role.values()).hasSize(2);
    }

    // ================= MessageRole =================

    @Test
    void messageRole_hasExpectedValues() {
        assertThat(MessageRole.values()).containsExactly(
                MessageRole.USER,
                MessageRole.AI
        );
    }

    @ParameterizedTest
    @CsvSource({
            "USER, USER",
            "AI, AI"
    })
    void messageRole_fromString(String input, MessageRole expected) {
        assertThat(MessageRole.valueOf(input)).isEqualTo(expected);
    }

    @Test
    void messageRole_count() {
        assertThat(MessageRole.values()).hasSize(2);
    }

    // ================= ImageSize =================

    @Test
    void imageSize_hasExpectedValues() {
        assertThat(ImageSize.values()).containsExactly(
                ImageSize.SQUARE,
                ImageSize.LANDSCAPE,
                ImageSize.PORTRAIT
        );
    }

    @ParameterizedTest
    @CsvSource({
            "SQUARE, SQUARE",
            "LANDSCAPE, LANDSCAPE",
            "PORTRAIT, PORTRAIT"
    })
    void imageSize_fromString(String input, ImageSize expected) {
        assertThat(ImageSize.valueOf(input)).isEqualTo(expected);
    }

    @Test
    void imageSize_count() {
        assertThat(ImageSize.values()).hasSize(3);
    }
}
