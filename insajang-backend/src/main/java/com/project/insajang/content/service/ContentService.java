package com.project.insajang.content.service;

import com.project.insajang.content.dto.ContentCreateRequest;
import com.project.insajang.content.entity.Content;
import com.project.insajang.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // 파이썬 통신용 도구

    public Map<String, String> processAndSaveContent(ContentCreateRequest request, String userId) {

        // 1. 파이썬 서버(AI)에 데이터 전송 및 결과 수신
        String pythonUrl = "http://localhost:8000/generate"; // 파이썬 서버 주소

        // 파이썬이 받을 형식에 맞춰 데이터 조립
        Map<String, Object> pythonRequest = new HashMap<>();
        pythonRequest.put("title", request.getTitle());
        pythonRequest.put("user_input", request.getUser_input());
        pythonRequest.put("content_type", request.getContent_type());

        // 파이썬에게 일을 시키고 결과를 받아옵니다.
        // 파이썬 응답 예시: {"generated_text": "안녕! 인스타사장님이야..."}
        Map<String, String> pythonResponse = restTemplate.postForObject(pythonUrl, pythonRequest, Map.class);
        String generatedResult = pythonResponse.get("generated_text");

        // 2. 받은 결과를 우리 DB(PostgreSQL)에 저장
        Content content = new Content();
        content.setProjectId(request.getProject_id());
        content.setTitle(request.getTitle());
        content.setUserInput(request.getUser_input()); // 요청 정보 저장!
        content.setBody(generatedResult);             // AI 결과 저장!
        content.setContentType(request.getContent_type());
        content.setType(request.getContent_type());    // 필요시 구분값 설정
        content.setStatus("DRAFT");                    // 기본값 설정

        contentRepository.save(content); // DB에 쏙!

        // 3. 리액트가 기다리는 형식으로 결과 반환
        Map<String, String> result = new HashMap<>();
        result.put("generated_text", generatedResult);

        return result;
    }
}