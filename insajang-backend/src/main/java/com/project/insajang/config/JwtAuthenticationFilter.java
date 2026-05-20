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

        // 1. 토큰이 존재할 때
        if (token != null) {
            // 토큰이 유효한 경우 ➡️ 정상 인증 처리
            if (jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("인증 성공: " + auth.getName());
                filterChain.doFilter(request, response); // 다음 단계로 진행
            }
            // 💡 2. 토큰이 있는데 만료되었거나 유효하지 않은 경우 ➡️ 즉시 401 응답 거부!
            else {
                System.out.println("인증 실패: 만료되거나 변조된 토큰입니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 세팅
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"토큰이 만료되었습니다.\"}");
                return; // 🔥 중요: 여기서 return을 해서 다음 filterChain을 타지 못하게 막아야 합니다!
            }
        }
        // 3. 토큰이 아예 없는 경우 (ex: 로그인, 회원가입 화면 등)
        else {
            System.out.println("토큰 없음: 공용 API 요청 또는 최초 접근");
            filterChain.doFilter(request, response); // SecurityConfig의 permitAll 믿고 일단 통과
        }
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