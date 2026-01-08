package com.project.insajang.service;

import com.project.insajang.entity.User;
import com.project.insajang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 페이스북 토큰을 이용해 인스타그램 계정 정보를 캐내는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramApiService {

    private final UserRepository userRepository;


    private final RestTemplate restTemplate;

    /**
     * 사용자의 페이스북 페이지와 연결된 인스타그램 계정 ID를 찾아옵니다.
     */
    public void getAndSaveInstagramInfo(User user) {
        String accessToken = user.getAccessToken();
        try {
            // 1. 페이스북 "나의 페이지 목록" 조회 API 주소 생성
            String url = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v18.0/me/accounts")
                    .queryParam("access_token", accessToken)
                    .toUriString();
            System.out.println(url);
            // 2. API 호출
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

            if (data != null && !data.isEmpty()) {
                String pageId = (String) data.get(0).get("id");
                log.info("### 연결된 페이지 ID 발견: {} ###", pageId);

                // 3. 페이지 ID를 이용해 다시 "인스타그램 비즈니스 계정 ID" 조회
                String igUrl = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v18.0/" + pageId)
                        .queryParam("fields", "instagram_business_account")
                        .queryParam("access_token", accessToken)
                        .toUriString();

                Map<String, Object> igResponse = restTemplate.getForObject(igUrl, Map.class);
                Map<String, Object> igAccount = (Map<String, Object>) igResponse.get("instagram_business_account");

                if (igAccount != null) {
                    String igId = (String) igAccount.get("id");
                    log.info("### 인스타그램 비즈니스 계정 ID 발굴 성공: {} ###", igId);

                    // 4. 찾은 정보를 DB에 저장
                    user.setLinkedPageId(pageId);
                    user.setInstagramBusinessAccountId(igId);
                    userRepository.save(user);
                }
            }
        } catch (Exception e) {
            log.error("### 인스타 정보 캐내기 실패: {} ###", e.getMessage());
        }
    }
}