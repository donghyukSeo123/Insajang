package com.project.insajang.config;

import com.project.insajang.user.entity.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final String secretString = "contents-maker-studio-secure-key-2026-auth";
    private final Key secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
    private final long validityInMilliseconds = 3600000;

    // 토큰 생성 (userId 주머니에 넣기)
    public String createToken(Long userId, String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);
        claims.put("role",role );

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 토큰 만들 때 쓴 그 비밀키!
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되어도 그 안의 데이터는 꺼낼 수 있게 예외 처리를 해줍니다.
            return e.getClaims();
        }
    }

    // 토큰에서 userId 꺼내기
    public Long getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }


    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    // 토큰에서 role만 빼오는 보조 메서드 예시
    private String getRoleFromToken(String token) {
        Claims claims = parseClaims(token); // 이전에 만든 해독 메서드
        return claims.get("role").toString(); // DB에 'ROLE_USER'로 저장했으니 그대로 나옵니다.
    }


    public Authentication getAuthentication(String token) {
        // 1. 이미 만드신 메서드들을 활용해 정보를 꺼냅니다.
        Long userId = getUserId(token);
        String email = getEmail(token);
        String role = getRoleFromToken(token); // DB에서 'ROLE_USER'로 가져온 값

        // 2. [핵심] 빈 리스트(emptyList) 대신 실제 권한을 담습니다.
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(role));

        // 3. UserPrincipal 객체를 생성합니다.
        // (컨트롤러에서 @AuthenticationPrincipal로 꺼내 쓰기 위함)
        UserPrincipal principal = new UserPrincipal(userId, email, authorities);

        // 4. [중요] 마지막 인자에 반드시 authorities를 넣어주세요!
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 💡 토큰 유효기간이 만료되면 로그를 남기고 false 리턴
            System.out.println(" 만료된 JWT 토큰입니다. (1시간 초과)");
            return false;
        } catch (Exception e) {
            // 그 외 위조되거나 잘못된 토큰 처리
            return false;
        }
    }
}