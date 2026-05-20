package com.project.insajang.content.entity;

import com.project.insajang.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents", schema = "public")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 프로젝트와 연결 (기존 projects 테이블의 project_id 참조)
    @Column(name = "project_id")
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

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "log_id")
    private Long logId; // 생성의 근거가 된 로그 ID

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files = new ArrayList<>();

    // ================= 💡 [추가] 메일 링크 보안용 토큰 필드만 추가 =================
    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "token_generated_at")
    private LocalDateTime tokenGeneratedAt; // 💡 토큰 생성 시간 필드 추가

    // 비즈니스 로직에 따른 상태 변경 편의 메서드
    public void publishNotificationSent(String token) {
        this.status = "PUBLISHED";       // 메일 발송(=유저 노출) 완료 시 완료 상태로 전이
        this.verificationToken = token;  // 보안 토큰 저장
        this.tokenGeneratedAt = LocalDateTime.now();
    }

}