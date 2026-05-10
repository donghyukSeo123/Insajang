package com.project.insajang.content.entity;

import lombok.Getter;

@Getter
public enum ContentStatus {
    DRAFT("임시저장"),
    SCHEDULED("예약중"),
    PUBLISHED("발행완료"),
    FAILED("발행실패");

    private final String description;

    ContentStatus(String description) {
        this.description = description;
    }

}