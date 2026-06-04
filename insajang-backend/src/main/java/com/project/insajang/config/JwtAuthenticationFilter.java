package com.project.insajang.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [컨텐츠메이커스튜디오 문지기]
 * 모든 API 요청마다 토큰이 있는지 확인하고, 있으면 유저 정보를 가방(Context)에 담아줍니다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        System.out.println("문지기가 받은 토큰: " + token);

        // 토큰이 존재하고 유효한 경우에만 인증 객체를 SecurityContext에 세팅
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("인증 성공: " + auth.getName());
        }

        // 항상 필터 체인을 계속 진행시킵니다.
        // 인증정보가 필요한 API는 SecurityConfig 설정에 따라 Spring Security가 자동으로 401/403으로 차단합니다.
        filterChain.doFilter(request, response);
    }

    // 헤더에서 토큰만 쏙 빼오는 보조 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 값만 반환
        }
        return null;
    }
}