package com.project.insajang.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTreeDTO {
   private long projectId;
   private String name;
   private final String type = "folder"; // 🚀 프론트에서 아이콘 구분용
   private List<ContentTreeDTO> children; //프론트 재귀함수
}
