package com.project.insajang.service;

import com.project.insajang.dto.InstagramInsightResponse;
import com.project.insajang.entity.InstagramInsight;
import com.project.insajang.entity.User;
import com.project.insajang.repository.InstagramInsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InstagramInsightService {

    private final InstagramInsightRepository insightRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void updateMonthlyInsights(User user) {
        String businessId = user.getInstagramBusinessAccountId();
        String token = user.getAccessToken();

        // 1. 인사이트 API 호출 (최근 1일치 impressions, reach, profile_views)
        String url = "https://graph.facebook.com/v18.0/" + businessId +
                "/insights?metric=impressions,reach,profile_views&period=day&access_token=" + token;

        InstagramInsightResponse response = restTemplate.getForObject(url, InstagramInsightResponse.class);

        if (response != null && response.getData() != null) {
            InstagramInsight insight = new InstagramInsight();
            insight.setUser(user);
            insight.setRecordedAt(LocalDateTime.now());

            // 2. JSON 데이터에서 값 추출하여 엔티티에 세팅
            response.getData().forEach(item -> {
                int value = item.getValues().get(0).getValue();
                switch (item.getName()) {
                    case "impressions": insight.setImpressions(value); break;
                    case "reach": insight.setReach(value); break;
                    case "profile_views": insight.setProfileViews(value); break;
                }
            });

            // 3. DB 저장
            insightRepository.save(insight);
        }
    }
}