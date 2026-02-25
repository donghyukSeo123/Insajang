package com.project.insajang.project.dto;

import com.project.insajang.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDto {

    private Long id;          // DB에서 자동 생성된 프로젝트 PK
    private String name;        // 프로젝트 이름
    private String description; // 프로젝트 설명
    private String status;      // 프로젝트 상태 (ACTIVE 등)
    private Long userId;        // 소유자 ID (인사장님 ID)

    public static ProjectResponseDto from(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getProjectId()) // getId()가 아니라 getProjectId()입니다!
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .userId(project.getUser().getId()) // User 엔티티의 PK 필드 확인 필요
                .build();
    }
}