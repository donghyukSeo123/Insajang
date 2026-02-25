package com.project.insajang.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateDto {


    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    @Size(max = 255, message = "이름은 255자 이하로 입력해주세요.")
    private String name;

    private String description;

    @NotBlank(message = "상태값은 필수입니다.")
    private String status; // 초기값은 보통 프론트에서 보내거나 Service에서 기본값(예: "ACTIVE") 설정
}