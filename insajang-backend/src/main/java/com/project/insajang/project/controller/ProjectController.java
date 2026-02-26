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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // 1. [GET] 전체 목록 조회
    @GetMapping("/getUserProjects")
    public ResponseEntity<List<ProjectResponseDto>> getUserProjects(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userId = String.valueOf(userPrincipal.getId()); // 인사장님 ID 추출
        List<ProjectResponseDto> list = projectService.getUserProjects(userId);
        return ResponseEntity.ok(list);
    }

    // 1. 프로젝트 생성 (POST /api/projects)
    @PostMapping("/createProject")
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody ProjectCreateDto createDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal // 로그인된 사용자 정보
    ) {

        log.info(String.valueOf(userPrincipal.getId()));
        // 서비스 계층에 비즈니스 로직 위임
        ProjectResponseDto response = projectService.saveProject(createDto, userPrincipal.getId());

        // 성공 시 201 Created 응답과 생성된 데이터 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{projectId}") // 1. 주소창의 {projectId}를 변수로 쓰겠다는 선언
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 2. 로그인한 인사장님 정보
            @PathVariable("projectId") Long projectId // 3. 주소에 딸려온 ID를 낚아챔
    ) {
        // 서비스에 '누가(User)' '무엇을(Project)' 지울 건지 전달
        projectService.deleteProject(userPrincipal.getId(), projectId);

        // 성공 시 204 No Content(내용 없음) 또는 200 OK 반환
        return ResponseEntity.noContent().build();
    }
}