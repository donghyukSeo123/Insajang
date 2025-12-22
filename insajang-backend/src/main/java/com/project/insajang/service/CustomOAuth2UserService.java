package com.project.insajang.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OAuth2 사용자 정보 처리 서비스
 * 리소스 서버(Facebook)로부터 전달받은 정보를 서비스 로직으로 연계합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser를 호출하여 사용자 정보(Attributes) 획득
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 인스타그램 API 연동에 필요한 Access Token 추출
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 데이터 추출 및 식별자 변환
        String facebookId = String.valueOf(attributes.get("id"));
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");

        log.info("OAuth2 Login Success: FacebookId={}, Name={}", facebookId, name);

        // 데이터베이스 영속화 처리
        userService.saveOrUpdate(facebookId, name, email, accessToken);

        return oAuth2User;
    }
}