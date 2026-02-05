package com.project.insajang.user.scheduler;

import com.project.insajang.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationCleaner {

    private final UserService userService;

    // 1분(60,000ms)마다 실행
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCodes() {
        userService.removeExpiredVerificationCodes();
    }
}
