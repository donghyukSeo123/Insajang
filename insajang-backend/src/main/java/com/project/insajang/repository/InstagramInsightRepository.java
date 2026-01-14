package com.project.insajang.repository;

import com.project.insajang.entity.InstagramInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstagramInsightRepository extends JpaRepository<InstagramInsight, Long> {
    
}