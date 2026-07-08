package com.project.insajang.content.scheduler;

import com.project.insajang.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicScheduler {

    private final ContentService contentService;

    // 매일 00:05분에 실행
    @Scheduled(cron = "0 5 0 * * *")
    public void runDailyTopicRecommendationSync() {
        log.info("⏰ [배치 작업] 오늘의 추천 주제 동기화 스케줄러 실행");
        LocalDate today = LocalDate.now();
        contentService.syncRecommendations(today);
    }
}
