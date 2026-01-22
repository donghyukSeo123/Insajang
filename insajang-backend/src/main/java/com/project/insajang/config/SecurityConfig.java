package com.project.insajang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API 요청은 인증 필요, 나머지는 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // CSRF 비활성화 (개발용)
                .csrf(csrf -> csrf.disable())
                // 스프링 기본 로그인 폼 비활성화
                .formLogin(form -> form
                        .loginPage("/login")          //로그인 페이지
                        .defaultSuccessUrl("/dashboard")
                        .permitAll()
                )

                // HTTP Basic 비활성화
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
