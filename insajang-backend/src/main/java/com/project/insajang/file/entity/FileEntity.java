package com.project.insajang.file.entity;

import com.project.insajang.content.entity.Content;
import com.project.insajang.content.entity.ContentLog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "files", schema = "public")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 생성 요청 로그와 연결 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private ContentLog contentLog;

    // 최종 게시글과 연결 (N:1, 발행 전까지 NULL 허용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(name = "saved_name", nullable = false, length = 255)
    private String savedName;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    @Column(name = "file_size")
    private Long fileSize;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "TEMPORARY";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 최종 게시글 발행 시 파일 상태를 영구 저장(PERMANENT)으로 변경하고 게시글 ID 연결
     */
    public void confirmContent(Content content) {
        this.content = content;
        this.status = "PERMANENT";
    }
}