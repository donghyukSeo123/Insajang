package com.project.insajang.content.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentSaveRequest {
    private Long projectId;
    private String logId;       // 어떤 로그에서 온 건지 추적용
    private String title;       // 최종 제목
    private String body;        // 사용자가 편집 완료한 최종 본문
    private String contentType;
}