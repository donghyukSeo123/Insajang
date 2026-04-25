package com.project.insajang.content.dto;

import lombok.*;

/**
 * 컨텐츠 수정을 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentUpdateDto {

    private String title;

    private String body;

    private String category;

    private String imageUrl;

    private Boolean isVisible;

}