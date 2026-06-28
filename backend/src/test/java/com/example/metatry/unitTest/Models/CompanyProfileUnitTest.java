package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyProfileUnitTest {

    @Test
    void builder_setsAllFields() {
        CompanyProfile profile = CompanyProfile.builder()
                .id(1L)
                .companyName("Acme Corp")
                .instagramUrl("https://instagram.com/acme")
                .facebookUrl("https://facebook.com/acme")
                .linkedinUrl("https://linkedin.com/company/acme")
                .build();

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(profile.getInstagramUrl()).isEqualTo("https://instagram.com/acme");
        assertThat(profile.getFacebookUrl()).isEqualTo("https://facebook.com/acme");
        assertThat(profile.getLinkedinUrl()).isEqualTo("https://linkedin.com/company/acme");
    }

    @Test
    void noArgsConstructor_createsEmpty() {
        CompanyProfile profile = new CompanyProfile();
        assertThat(profile.getId()).isNull();
        assertThat(profile.getCompanyName()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        CompanyProfile profile = new CompanyProfile(
                1L, "Company", "https://ig.com/c",
                "https://fb.com/c", "https://li.com/c",
                null, null
        );

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getCompanyName()).isEqualTo("Company");
        assertThat(profile.getInstagramUrl()).isEqualTo("https://ig.com/c");
        assertThat(profile.getFacebookUrl()).isEqualTo("https://fb.com/c");
        assertThat(profile.getLinkedinUrl()).isEqualTo("https://li.com/c");
    }

    @Test
    void prePersist_setsTimestamps() {
        CompanyProfile profile = new CompanyProfile();
        profile.onCreate();

        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
        assertThat(profile.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(profile.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void preUpdate_updatesUpdatedAt() {
        CompanyProfile profile = new CompanyProfile();
        profile.onCreate();
        LocalDateTime original = profile.getUpdatedAt();

        profile.onUpdate();

        assertThat(profile.getUpdatedAt()).isAfterOrEqualTo(original);
    }

    @Test
    void setters_updateFields() {
        CompanyProfile profile = new CompanyProfile();

        profile.setCompanyName("New Name");
        profile.setInstagramUrl("https://ig.com/new");
        profile.setFacebookUrl("https://fb.com/new");

        assertThat(profile.getCompanyName()).isEqualTo("New Name");
        assertThat(profile.getInstagramUrl()).isEqualTo("https://ig.com/new");
        assertThat(profile.getFacebookUrl()).isEqualTo("https://fb.com/new");
    }

    @Test
    void nullFields_areHandled() {
        CompanyProfile profile = CompanyProfile.builder().build();

        assertThat(profile.getId()).isNull();
        assertThat(profile.getCompanyName()).isNull();
        assertThat(profile.getInstagramUrl()).isNull();
        assertThat(profile.getFacebookUrl()).isNull();
        assertThat(profile.getLinkedinUrl()).isNull();
    }

    @Test
    void createdAt_remainsNull_whenNotPersisted() {
        CompanyProfile profile = CompanyProfile.builder().companyName("Test").build();
        assertThat(profile.getCreatedAt()).isNull();
    }

    @Test
    void updatedAt_remainsNull_whenNotPersisted() {
        CompanyProfile profile = CompanyProfile.builder().companyName("Test").build();
        assertThat(profile.getUpdatedAt()).isNull();
    }
}
