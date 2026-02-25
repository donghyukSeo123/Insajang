package com.project.insajang.project.controller;

import com.project.insajang.project.dto.ProjectCreateDto;
import com.project.insajang.project.dto.ProjectResponseDto;
import com.project.insajang.project.service.ProjectService;
import com.project.insajang.user.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // 1. 프로젝트 생성 (POST /api/projects)
    @PostMapping("/createProject")
    public ResponseEntity<ProjectCreateDto> createProject(
            @RequestBody ProjectCreateDto createDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal // 로그인된 사용자 정보
    ) {

        log.info(String.valueOf(userPrincipal.getId()));
        // 서비스 계층에 비즈니스 로직 위임
        ProjectResponseDto response = projectService.saveProject(createDto, userPrincipal.getId());

        // 성공 시 201 Created 응답과 생성된 데이터 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 2. 내 프로젝트 목록 조회 (GET /api/projects)
    /*@GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getMyProjects(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<ProjectResponseDto> projects = projectService.findAllByUserId(userPrincipal.getId());
        return ResponseEntity.ok(projects);
    }*/
}