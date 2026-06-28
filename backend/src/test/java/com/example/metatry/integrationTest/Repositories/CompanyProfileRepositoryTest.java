package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CompanyProfileRepositoryTest {

    @Autowired private CompanyProfileRepository companyProfileRepository;

    @BeforeEach
    void setUp() {
        companyProfileRepository.deleteAll();
        companyProfileRepository.save(CompanyProfile.builder()
                .companyName("Acme")
                .instagramUrl("https://ig.com/acme")
                .facebookUrl("https://fb.com/acme")
                .linkedinUrl("https://li.com/acme").build());
    }

    @Test
    void findByCompanyName() {
        assertThat(companyProfileRepository.findByCompanyName("Acme")).isPresent();
    }

    @Test
    void existsByCompanyName() {
        assertThat(companyProfileRepository.existsByCompanyName("Acme")).isTrue();
    }
}
