package com.project.insajang.config; // 패키지 경로는 프로젝트에 맞게!

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 모든 주소에 대해
                .allowedOrigins("http://localhost:3000") // 2. 3000번 포트 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 3. 주요 방식들 허용
                .allowedHeaders("*"); // 4. 모든 헤더 허용
    }

    // 2. 정적 리소스 경로 매핑 (이미지 표시를 위해 필수!)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 물리 경로 추출 (예: C:/workspace/Insajang/uploads)
        Path path = Paths.get(uploadDir).toAbsolutePath();
        String resourcePath = "file:///" + path.toString() + "/";

        registry.addResourceHandler("/uploads/**") // 브라우저가 /uploads/로 시작하는 주소를 요청하면
                .addResourceLocations(resourcePath); // 실제 하드디스크의 해당 폴더를 뒤져라!
    }
}