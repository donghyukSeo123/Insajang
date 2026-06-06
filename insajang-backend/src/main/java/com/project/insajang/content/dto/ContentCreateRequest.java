package com.project.insajang.content.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ContentCreateRequest {
    private Long project_id;
    private String title;
    private String user_input;
    private String content_type;
    private String selectedPersona;
}