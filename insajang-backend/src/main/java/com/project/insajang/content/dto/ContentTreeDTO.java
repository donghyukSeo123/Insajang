package com.project.insajang.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTreeDTO {
    private Long contentId;
    private String title;

    private final String type = "file"; // 🚀 프론트에서 파일 아이콘 표시용
    // 바로 상세 내용을 보여주고 싶다면 상세 정보 DTO를 포함
    private ContentResponse detail;
}
