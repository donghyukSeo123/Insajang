package com.project.insajang.content.controller;


import com.project.insajang.content.dto.*;
import com.project.insajang.content.entity.TopicRecommendation;
import com.project.insajang.content.service.ContentService;
import com.project.insajang.user.entity.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
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

    @PostMapping("/generateAdditionalImage")
    public ResponseEntity<?> generateAdditionalImage(
            @RequestBody AdditionalImageRequest request
    ) throws IOException {
        String imageUrl = contentService.generateAndSaveAdditionalImage(request);
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.ok(response);
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

    @GetMapping("/recommendations")
    public ResponseEntity<List<TopicRecommendation>> getTodayRecommendations() {
        List<TopicRecommendation> recommendations = contentService.getTodayRecommendations();
        return ResponseEntity.ok(recommendations);
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

    /**
     * 컨텐츠 수정 (PATCH: 부분 수정)
     */
    @PatchMapping("/update/{contentId}")
    public ResponseEntity<?> updateContent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long contentId,
            @RequestBody ContentUpdateDto updateDto) {

        try {
            // 1. Service를 통해 비즈니스 로직 및 DB 업데이트 수행
            ContentResponse content = contentService.update(contentId, updateDto);

            // 2. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "성공적으로 수정되었습니다.",
                    "data", content
            ));

        } catch (EntityNotFoundException e) {
            // 해당 ID가 없을 경우 404 에러
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "컨텐츠를 찾을 수 없습니다."));
        } catch (Exception e) {
            // 기타 서버 에러 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 컨텐츠 예약 저장 및 상태 변경
     * @param request
     * 예약 시간(scheduledAt)과 컨텐츠 정보 포함
     */
    @PostMapping("/save-schedule") // 케밥 케이스(kebab-case) 관례 적용 추천
    public ResponseEntity<ContentResponse> saveSchedule(
            @RequestBody ContentSaveRequest request, // 최종 저장용 DTO (id, title, text 포함)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info(request.getContentId());
        log.info(String.valueOf(request.getScheduledAt()));

        ContentResponse response = contentService.saveSchedule(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 컨텐츠 예약 일정 취소 (캘린더에서 제거)
     */
    @PostMapping("/cancel-schedule/{contentId}")
    public ResponseEntity<ContentResponse> cancelSchedule(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ContentResponse response = contentService.cancelSchedule(contentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 1. 캘린더 화면 범위에 따른 일정 목록 조회
     * @param start 조회 시작일 (FullCalendar에서 전달)
     * @param end   조회 종료일 (FullCalendar에서 전달)
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<ContentResponse>> getSchedules(
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("일정 조회 요청: {} ~ {}", start, end);
        String userId = String.valueOf(userPrincipal.getId());
        // 서비스에서 기간 내의 일정을 조회 (필요 시 userPrincipal을 통해 본인 데이터만 필터링)
        List<ContentResponse> schedules = contentService.getSchedulesByRange(start, end, userId);

        return ResponseEntity.ok(schedules);
    }


}