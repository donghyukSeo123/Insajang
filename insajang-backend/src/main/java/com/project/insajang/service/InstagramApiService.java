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
            // 1. 페이지 목록을 가져올 때 'instagram_business_account' 필드를 명시적으로 요청합니다.
            String url = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v18.0/me/accounts")
                    .queryParam("fields", "name,id,instagram_business_account") // 이 부분이 핵심!
                    .queryParam("access_token", accessToken)
                    .toUriString();

            log.info("### 페이지 목록 조회 URL: {} ###", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

            if (data != null && !data.isEmpty()) {
                // 여러 페이지 중 인스타 계정이 연결된 첫 번째 페이지를 찾습니다.
                for (Map<String, Object> page : data) {
                    String pageId = (String) page.get("id");
                    Map<String, Object> igAccount = (Map<String, Object>) page.get("instagram_business_account");

                    if (igAccount != null) {
                        String igId = (String) igAccount.get("id");
                        log.info("### [성공] 페이지명: {}, 인스타ID: {} ###", page.get("name"), igId);

                        user.setLinkedPageId(pageId);
                        user.setInstagramBusinessAccountId(igId);
                        userRepository.save(user);
                        return; // 찾았으면 종료
                    }
                }
                log.warn("### 연결된 페이지는 찾았으나, 그 페이지에 연결된 인스타 비즈니스 계정이 없습니다. ###");
            } else {
                log.warn("### 이 계정이 관리자로 등록된 페이스북 페이지가 하나도 없습니다. ###");
            }
        } catch (Exception e) {
            log.error("### 인스타 정보 캐내기 에러: {} ###", e.getMessage());
        }
    }
}