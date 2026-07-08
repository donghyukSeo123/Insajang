package com.project.insajang.content.repository;

import com.project.insajang.content.entity.TopicRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TopicRecommendationRepository extends JpaRepository<TopicRecommendation, Long> {
    List<TopicRecommendation> findByRecommendDate(LocalDate recommendDate);
}
