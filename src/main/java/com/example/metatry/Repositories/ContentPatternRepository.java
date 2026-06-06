package com.example.metatry.Repositories;

import com.example.metatry.Models.ContentPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentPatternRepository extends JpaRepository<ContentPattern, Long> {

    Optional<ContentPattern> findByTopic(String topic);

    List<ContentPattern> findByTopicContainingIgnoreCase(String keyword);

    List<ContentPattern> findByCompanyName(String companyName);

    List<ContentPattern> findTop3ByOrderByExtractedAtDesc();
}