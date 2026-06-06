package com.example.metatry.Repositories;

import com.example.metatry.Models.PostMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostMetricRepository extends JpaRepository<PostMetric, Long> {

    List<PostMetric> findByPostIdOrderByCollectedAtAsc(Long postId);

    Optional<PostMetric> findTopByPostIdOrderByCollectedAtDesc(Long postId);

}