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

        // 1. 요청 헤더(Authorization)에서 "Bearer [토큰]"을 꺼내옵니다.
        String token = resolveToken(request);

        // 2. 토큰이 비어있지 않고, 가짜가 아닌지 검증합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰이 진짜라면, 유저 정보(Authentication)를 가져옵니다.
            Authentication auth = jwtTokenProvider.getAuthentication(token);

            // 4. ⭐ 중요: 이 유저가 인증되었다는 사실을 스프링 보관함에 저장합니다.
            // 이렇게 해야 컨트롤러에서 @AuthenticationPrincipal을 쓸 수 있습니다!
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 5. 다음 문지기(필터)에게 넘깁니다.
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