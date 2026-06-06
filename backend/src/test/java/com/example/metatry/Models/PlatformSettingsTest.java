package com.example.metatry.Models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformSettingsTest {

    @Test
    void noArgsConstructor_setsDefaults() {
        PlatformSettings settings = new PlatformSettings();
        assertThat(settings.isActive()).isTrue();
        assertThat(settings.getId()).isNull();
    }

    @Test
    void setters_updateAllFields() {
        PlatformSettings settings = new PlatformSettings();

        settings.setId(1L);
        settings.setPlatformName("FACEBOOK");
        settings.setPageId("fb_page_123");
        settings.setPageName("My Page");
        settings.setAccessToken("EAAToken123");
        settings.setActive(false);
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());

        assertThat(settings.getId()).isEqualTo(1L);
        assertThat(settings.getPlatformName()).isEqualTo("FACEBOOK");
        assertThat(settings.getPageId()).isEqualTo("fb_page_123");
        assertThat(settings.getPageName()).isEqualTo("My Page");
        assertThat(settings.getAccessToken()).isEqualTo("EAAToken123");
        assertThat(settings.isActive()).isFalse();
        assertThat(settings.getCreatedAt()).isNotNull();
        assertThat(settings.getUpdatedAt()).isNotNull();
    }

    @Test
    void active_defaultIsTrue() {
        PlatformSettings settings = new PlatformSettings();
        assertThat(settings.isActive()).isTrue();

        settings.setActive(false);
        assertThat(settings.isActive()).isFalse();
    }

    @Test
    void gettersAndSetters_roundTrip() {
        PlatformSettings settings = new PlatformSettings();

        settings.setPlatformName("LINKEDIN");
        settings.setPageId("li_page_456");
        settings.setAccessToken("AT_secret");

        assertThat(settings.getPlatformName()).isEqualTo("LINKEDIN");
        assertThat(settings.getPageId()).isEqualTo("li_page_456");
        assertThat(settings.getAccessToken()).isEqualTo("AT_secret");
    }

    @Test
    void nullValues_areHandled() {
        PlatformSettings settings = new PlatformSettings();

        assertThat(settings.getId()).isNull();
        assertThat(settings.getPlatformName()).isNull();
        assertThat(settings.getPageId()).isNull();
        assertThat(settings.getPageName()).isNull();
        assertThat(settings.getAccessToken()).isNull();
        assertThat(settings.getCreatedAt()).isNull();
        assertThat(settings.getUpdatedAt()).isNull();
        assertThat(settings.isActive()).isTrue();
    }

    @Test
    void createdAt_remainsNull_whenNotSet() {
        PlatformSettings settings = new PlatformSettings();
        assertThat(settings.getCreatedAt()).isNull();
    }

    @Test
    void updatedAt_remainsNull_whenNotSet() {
        PlatformSettings settings = new PlatformSettings();
        assertThat(settings.getUpdatedAt()).isNull();
    }

    @Test
    void multiplePlatforms() {
        PlatformSettings fb = new PlatformSettings();
        fb.setPlatformName("FACEBOOK");

        PlatformSettings li = new PlatformSettings();
        li.setPlatformName("LINKEDIN");

        assertThat(fb.getPlatformName()).isNotEqualTo(li.getPlatformName());
    }

    @Test
    void activeToggled() {
        PlatformSettings settings = new PlatformSettings();
        assertThat(settings.isActive()).isTrue();

        settings.setActive(false);
        assertThat(settings.isActive()).isFalse();

        settings.setActive(true);
        assertThat(settings.isActive()).isTrue();
    }
}
