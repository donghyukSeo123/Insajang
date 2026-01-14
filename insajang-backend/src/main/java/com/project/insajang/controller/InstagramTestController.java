package com.project.insajang.controller;

import com.project.insajang.entity.User;
import com.project.insajang.repository.UserRepository;
import com.project.insajang.service.InstagramInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j // 로그 출력을 위해 추가
@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 리액트 허용
public class InstagramTestController {

    private final UserRepository userRepository;
    private final InstagramInsightService insightService;

    @GetMapping("/sync/{userId}")
    public ResponseEntity<String> syncTest(@PathVariable Long userId) {
        log.info("동기화 시작 - 요청 유저 ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            // 우리가 만든 서비스 호출!
            insightService.updateMonthlyInsights(user);

            log.info("동기화 성공 - {}님의 데이터가 DB에 저장되었습니다.", user.getName());
            return ResponseEntity.ok("동기화 성공!");
        } catch (Exception e) {
            log.error("동기화 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("실패: " + e.getMessage());
        }
    }
}