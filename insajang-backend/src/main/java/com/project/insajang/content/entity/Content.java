package com.project.insajang.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contents", schema = "public")
@Getter @Setter
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    // 프로젝트와 연결 (기존 projects 테이블의 project_id 참조)
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    private String title;

    // 사용자가 입력한 요청 키워드 (이미 DB에 있는 컬럼)
    @Column(name = "user_input", columnDefinition = "TEXT")
    private String userInput;

    // AI가 생성한 최종 결과물 (DB의 body 컬럼과 매핑)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "content_type", length = 50)
    private String contentType; // INSTA, BLOG 등

    @Column(length = 50)
    private String status = "DRAFT"; // 기본값 DRAFT

    @Column(name = "type")
    private String type; // 컨텐츠 상세 분류

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}