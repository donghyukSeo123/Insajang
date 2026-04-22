package com.project.insajang.content.controller;


import com.project.insajang.content.dto.ContentCreateRequest;
import com.project.insajang.content.dto.ContentResponse;
import com.project.insajang.content.dto.ContentSaveRequest;
import com.project.insajang.content.dto.ProjectTreeDTO;
import com.project.insajang.content.service.ContentService;
import com.project.insajang.user.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/createContent")
    public ResponseEntity<?> createContent(
            @RequestBody ContentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal // 🛡️ 시큐리티가 채워준 유저 정보
    ) throws IOException {
        String userId = String.valueOf(userPrincipal.getId());

        // 2. 서비스 호출 (파이썬 AI 요청 + DB 저장 일괄 처리)
        // 리액트가 사용하는 키값인 "generated_text"를 포함한 Map을 반환합니다.
        Map<String, String> result = contentService.processAndSaveContentlog(request, userId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/saveGeneratedContent")
    public ResponseEntity<?> saveGeneratedContent(
            @RequestBody ContentSaveRequest request, // 최종 저장용 DTO (id, title, text 포함)
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String userId = String.valueOf(userPrincipal.getId());

        // 1. 서비스 레이어 호출 (로그 ID를 참조하여 최종 컨텐츠 저장)
        // request 안에는 사용자가 최종 수정한 title과 text가 들어있어야 합니다.
        ContentResponse result = contentService.finalizeAndSaveContent(request, userId);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/selectContentsTree")
    public ResponseEntity<List<ProjectTreeDTO>> getContentTree(
            @AuthenticationPrincipal UserPrincipal userPrincipal // 🛡️ 본인 데이터만 조회
    ) {
        String userId = String.valueOf(userPrincipal.getId());

        // 서비스에서 해당 유저의 프로젝트별/타입별 계층 구조를 조립
        List<ProjectTreeDTO> treeList = contentService.makeTreeStructure(userId);

        return ResponseEntity.ok(treeList);
    }

    @GetMapping("/detail/{contentId}")
    public ResponseEntity<ContentResponse> getContentDetail(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal // 보안을 위해 본인 소유인지 체크 권장
    ) {
        String userId = String.valueOf(userPrincipal.getId());

        // 1. 서비스에서 해당 유저의 특정 콘텐츠 상세 정보 조회
        // (보안상 userId와 contentId를 같이 넘겨 본인 것만 조회하게 하는 것이 좋습니다)
        ContentResponse detail = contentService.getContentDetail(userId, contentId);

        if (detail != null) {
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{contentId}") // 1. 주소창의 {projectId}를 변수로 쓰겠다는 선언
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 2. 로그인한 인사장님 정보
            @PathVariable("contentId") Long contentId // 3. 주소에 딸려온 ID를 낚아챔
    ) {
        String userId = String.valueOf(userPrincipal.getId());

        contentService.deleteContent(userId, contentId);

        // 성공 시 204 No Content(내용 없음) 또는 200 OK 반환
        return ResponseEntity.noContent().build();
    }


}