/* src/main/java/com/project/insajang/content/repository/ContentRepository.java */

package com.project.insajang.content.repository;

import com.project.insajang.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // 1. 특정 프로젝트에 속한 모든 컨텐츠 조회 (트리 구조 조립용)
    // 엔티티 내에 프로젝트 필드명이 'project'라고 가정할 때:
    List<Content> findByProjectId(Long projectId);

    Content findByContentId(Long contentId);

    List<Content> findByScheduledAtBetweenAndUserId(LocalDateTime startDate, LocalDateTime endDate, Long userId);
}