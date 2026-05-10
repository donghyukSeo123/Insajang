package com.project.insajang.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects", schema = "public")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    // 1. 프로젝트를 소유한 사용자 정보 (객체 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.project.insajang.user.entity.User user; // 유저 엔티티로 연결!

    // 2. 프로젝트의 실제 이름 (문자열)
    @Column(nullable = false, length = 255)
    private String name; // 프로젝트 이름은 이름대로 따로 있어야 합니다.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- 비즈니스 로직 메서드 ---
    public void updateProject(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }
}