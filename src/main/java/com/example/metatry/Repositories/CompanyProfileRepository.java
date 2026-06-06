package com.example.metatry.Repositories;

import com.example.metatry.Models.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByCompanyName(String companyName);
    boolean existsByCompanyName(String companyName);
}