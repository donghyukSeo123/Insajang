package com.project.insajang.content.service;

import com.project.insajang.content.dto.ContentCreateRequest;
import com.project.insajang.content.entity.ContentLog;
import com.project.insajang.content.repository.ContentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentLogRepository contentLogRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // 파이썬 통신용 도구

    public Map<String, String> processAndSaveContentlog(ContentCreateRequest request, String userId) {

        // 1. 파이썬 서버(AI)에 데이터 전송 및 결과 수신
        String pythonUrl = "http://localhost:8000/generate-content";

        Map<String, Object> pythonRequest = new HashMap<>();
        pythonRequest.put("title", request.getTitle());
        pythonRequest.put("user_input", request.getUser_input());
        pythonRequest.put("content_type", request.getContent_type());

        // AI 결과 수신
        Map<String, Object> pythonResponse = restTemplate.postForObject(pythonUrl, pythonRequest, Map.class);
        String generatedResult = String.valueOf(pythonResponse.get("generated_text"));
        String generatedTitle = String.valueOf(pythonResponse.get("generated_title"));
        // 2. [변경 포인트] 본 테이블이 아닌 '로그 테이블'에 먼저 저장합니다.
        // 사용자가 '최종 저장'을 누르기 전까지 본 테이블(Content)은 깨끗하게 유지됩니다.
        ContentLog log = ContentLog.builder()
                .projectId(request.getProject_id())
                .title(request.getTitle())
                .userInput(request.getUser_input())
                .generatedBody(generatedResult) // AI가 준 날것의 데이터
                .contentType(request.getContent_type())
                .type(request.getContent_type())
                .build();

        ContentLog savedLog = contentLogRepository.save(log);

        // 3. 리액트에게는 AI가 만든 텍스트만 돌려줍니다.
        Map<String, String> result = new HashMap<>();
        result.put("generated_text", generatedResult);
        result.put("generated_title", generatedTitle); // 🚀 리액트 'Final Post Title' 칸에 꽂아줄 제목
        result.put("log_id", String.valueOf(savedLog.getLogId())); // ID 추가!
        result.put("content_type", request.getContent_type());

        return result;
    }
}