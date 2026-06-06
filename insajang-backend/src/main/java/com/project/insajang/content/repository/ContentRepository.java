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

    /**
     * 예약 시각(scheduledAt)이 현재 시각 이하이면서
     * 현재 상태(status)가 'SCHEDULED'(게시예약)인 콘텐츠 목록만 딱 긁어옵니다.
     */
    List<Content> findByScheduledAtLessThanEqualAndStatus(LocalDateTime dateTime, String status);

    /**
     * 예약 시각(scheduledAt)이 시작 시각과 종료 시각 사이(예: 오늘 시작시각 ~ 현재시각)이면서
     * 현재 상태(status)가 'SCHEDULED'(게시예약)인 콘텐츠 목록만 긁어옵니다.
     */
    List<Content> findByScheduledAtBetweenAndStatus(LocalDateTime startDateTime, LocalDateTime endDateTime, String status);
}