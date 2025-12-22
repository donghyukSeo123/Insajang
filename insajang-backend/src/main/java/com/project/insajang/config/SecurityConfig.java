package com.project.insajang.config;

import com.project.insajang.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * HTTP 보안 정책, 인증 경로 설정 및 OAuth2 로그인 설정을 포함합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Stateless 환경(REST API) 대응을 위한 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        // 로그인 관련 엔드포인트 및 공통 리소스 허용
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // 사용자 정보 엔드포인트 설정 (Custom Service 연결)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 인증 성공 후 리다이렉션 경로 설정
                        .defaultSuccessUrl("http://localhost:3000/dashboard", true)
                );
        return http.build();
    }
}