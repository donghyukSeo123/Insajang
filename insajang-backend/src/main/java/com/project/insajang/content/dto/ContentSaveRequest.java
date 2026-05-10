package com.project.insajang.content.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ContentSaveRequest {
    private Long projectId;
    private String logId;       // 어떤 로그에서 온 건지 추적용
    private String title;       // 최종 제목
    private String body;        // 사용자가 편집 완료한 최종 본문
    private String contentType;
    private String status;
    // pattern을 프론트에서 보내주는 형식과 정확히 일치시킵니다.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime scheduledAt;
    private String contentId;
}