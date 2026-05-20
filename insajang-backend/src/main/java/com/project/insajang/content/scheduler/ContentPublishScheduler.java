package com.project.insajang.content.scheduler;

import com.project.insajang.content.entity.Content;
import com.project.insajang.content.repository.ContentRepository;
import com.project.insajang.email.service.EmailService;
import com.project.insajang.user.entity.User;
import com.project.insajang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentPublishScheduler {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000) // 1분마다 가동
    @Transactional // DB 상태 업데이트 안정성을 위해 추가
    public void checkAndNotifyReservedContents() {
        log.info("[콘텐츠 스케줄러] 예약 게시 알림 체크 시작...");
        LocalDateTime now = LocalDateTime.now();

        // 1. 현재 시간이 지났고, 상태가 'SCHEDULED'인 예약 타겟들 조회
        List<Content> targetContents = contentRepository.findByScheduledAtLessThanEqualAndStatus(now, "SCHEDULED");

        if (targetContents.isEmpty()) {
            log.info("[콘텐츠 스케줄러] 이번 주기에 발송할 예약 콘텐츠가 없습니다.");
            return;
        }

        for (Content content : targetContents) {
            try {
                // 2. 콘텐츠 작성자 정보 획득
                User user = userRepository.findById(content.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + content.getUserId()));

                String userEmail = user.getEmail();

                // 3. 일회성 보안 인증 토큰 및 링크 생성
                String token = UUID.randomUUID().toString();
                String publishLink = "https://contentsmakerstudio.com/publish?contentId="
                        + content.getContentId() + "&token=" + token;

                // 4. 의존성이 완전 분리된 메일 서비스 호출
                emailService.sendPublishLinkEmail(userEmail, content.getTitle(),publishLink);

                // 5. 성공 시 비즈니스 상태를 'PUBLISHED'로 변경하여 비즈니스 주기 완료 처리
                content.publishNotificationSent(token);
                contentRepository.save(content);

                log.info("[예약 완료] Content ID: {} -> PUBLISHED 상태 전환 및 메일 발송 성공", content.getContentId());
            } catch (Exception e) {
                log.error("[예약 처리 실패] Content ID: {} 처리 중 에러 발생", content.getContentId(), e);
                // 메일 서버 일시 오류 등의 예외 발생 시, 다음 1분 뒤 주기에 재시도할 수 있도록
                // status를 SCHEDULED로 그대로 둡니다. (save를 생략하거나 로깅만 남김)
            }
        }
        log.info("[콘텐츠 스케줄러] 예약 게시 알림 체크 종료.");
    }
}