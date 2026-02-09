package com.project.insajang.config; // 프로젝트 패키지 경로에 맞게 꼭 수정하세요!

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 컨텐츠메이커스튜디오 전용 시크릿 키 (32자 이상의 안전한 키)
    private final String secretString = "contents-maker-studio-secure-key-2026-auth";
    private final Key secretKey = Keys.hmacShaKeyFor(secretString.getBytes());

    // 토큰 유효 시간: 1시간
    private final long validityInMilliseconds = 3600000;

    /**
     * 유저의 이메일을 기반으로 엑세스 토큰을 생성합니다.
     */
    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256) // HMAC SHA256 알고리즘
                .compact();
    }

    /**
     * 토큰에서 유저 정보를 추출할 때 사용
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}