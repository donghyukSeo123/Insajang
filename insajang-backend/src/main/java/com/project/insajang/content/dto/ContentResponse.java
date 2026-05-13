package com.project.insajang.content.dto;

import com.project.insajang.content.entity.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    private Long projectId;
    private Long contentId;      // 생성된 본 테이블 ID
    private Long logId;          // 연결된 로그 ID
    private String title;        // 최종 저장된 제목
    private String body;         // 최종 저장된 본문 (HTML 포함 가능)
    private String contentType;  // BLOG, INSTA 등
    private String status;       // DRAFT, PUBLISHED 등
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime scheduledAt;

    // 💡 정적 팩토리 메서드 (엔티티를 DTO로 편하게 변환)
    public static ContentResponse fromEntity(Content content) {
        return ContentResponse.builder()
                .contentId(content.getContentId())
                .logId(content.getLogId())
                .title(content.getTitle())
                .body(content.getBody())
                .contentType(content.getContentType())
                .status(content.getStatus())
                .createdAt(content.getCreatedAt())
                .scheduledAt(content.getScheduledAt())
                .build();
    }
}