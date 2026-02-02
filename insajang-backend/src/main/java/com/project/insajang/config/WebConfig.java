package com.project.insajang.config; // 패키지 경로는 프로젝트에 맞게!

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 모든 주소에 대해
                .allowedOrigins("http://localhost:3000") // 2. 3000번 포트 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 3. 주요 방식들 허용
                .allowedHeaders("*"); // 4. 모든 헤더 허용
    }
}