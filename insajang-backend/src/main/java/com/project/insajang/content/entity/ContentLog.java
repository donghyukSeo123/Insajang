package com.project.insajang.content.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_logs", schema = "public")
@Getter
@NoArgsConstructor
public class ContentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    private String title;

    @Column(name = "user_input", columnDefinition = "TEXT")
    private String userInput;

    // AI가 생성한 '원본' 결과물
    @Column(name = "generated_body", nullable = false, columnDefinition = "TEXT")
    private String generatedBody;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "type")
    private String type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder // 생성 시 편리함을 위해 빌더 패턴 추가
    public ContentLog(Long projectId, String title, String userInput, String generatedBody, String contentType, String type, Long userId) {
        this.projectId = projectId;
        this.title = title;
        this.userInput = userInput;
        this.generatedBody = generatedBody;
        this.contentType = contentType;
        this.type = type;
        this.userId = userId;
    }
}