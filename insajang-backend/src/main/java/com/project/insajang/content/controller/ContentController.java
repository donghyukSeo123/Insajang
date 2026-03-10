package com.project.insajang.content.controller;


import com.project.insajang.content.dto.ContentCreateRequest;
import com.project.insajang.content.service.ContentService;
import com.project.insajang.user.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    ) {
        String userId = String.valueOf(userPrincipal.getId());

        // 2. 서비스 호출 (파이썬 AI 요청 + DB 저장 일괄 처리)
        // 리액트가 사용하는 키값인 "generated_text"를 포함한 Map을 반환합니다.
        Map<String, String> result = contentService.processAndSaveContentlog(request, userId);

        return ResponseEntity.ok(result);
    }
}