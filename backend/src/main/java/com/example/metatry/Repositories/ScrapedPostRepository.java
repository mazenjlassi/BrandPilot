package com.example.metatry.Repositories;

import com.example.metatry.Models.ScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {

    Optional<ScrapedPost> findByCompanyNameAndPlatformAndPostUrl(String companyName, String platform, String postUrl);

    Optional<ScrapedPost> findByCompanyNameAndPlatformAndPostText(String companyName, String platform, String postText);

    List<ScrapedPost> findByCompanyName(String companyName);

    List<ScrapedPost> findByPlatform(String platform);

    List<ScrapedPost> findByTopic(String topic);

    List<ScrapedPost> findByCompanyNameAndPlatform(String companyName, String platform);

    List<ScrapedPost> findByUsedForPatternFalse();

    long countByUsedForPatternFalse();

    List<ScrapedPost> findTop30ByUsedForPatternFalse();

    long countByCompanyNameAndUsedForPatternFalse(String companyName);

    List<ScrapedPost> findTop30ByCompanyNameAndUsedForPatternFalse(String companyName);

    long countByCompanyName(String companyName);

    long countByPlatform(String platform);

    @Query("SELECT DISTINCT p.companyName FROM ScrapedPost p WHERE p.companyName IS NOT NULL AND p.companyName <> ''")
    List<String> findDistinctCompanyNames();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE p1 FROM scraped_posts p1 " +
           "INNER JOIN scraped_posts p2 " +
           "WHERE p1.id > p2.id " +
           "AND p1.company_name = p2.company_name " +
           "AND p1.platform = p2.platform " +
           "AND p1.post_text = p2.post_text", nativeQuery = true)
    int deleteDuplicates();
}