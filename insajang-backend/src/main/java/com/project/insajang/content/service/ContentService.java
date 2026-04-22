package com.project.insajang.content.service;

import com.project.insajang.content.dto.*;
import com.project.insajang.content.entity.Content;
import com.project.insajang.content.entity.ContentLog;
import com.project.insajang.content.repository.ContentLogRepository;
import com.project.insajang.content.repository.ContentRepository;
import com.project.insajang.file.entity.FileEntity;
import com.project.insajang.file.repository.FileRepository;
import com.project.insajang.file.service.FileService;
import com.project.insajang.project.entity.Project;
import com.project.insajang.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentLogRepository contentLogRepository;
    private final ContentRepository contentRepository;
    private final ProjectRepository projectRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // 파이썬 통신용 도구
    private final FileService fileService;
    private final FileRepository fileRepository;

    public Map<String, String> processAndSaveContentlog(ContentCreateRequest request, String userId) throws IOException {

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
        String base64Data = String.valueOf(pythonResponse.get("img_data"));
        String extension = String.valueOf(pythonResponse.get("extension"));

        ContentLog log = ContentLog.builder()
                .projectId(request.getProject_id())
                .title(request.getTitle())
                .userInput(request.getUser_input())
                .generatedBody(generatedResult) // AI가 준 날것의 데이터
                .contentType(request.getContent_type())
                .type(request.getContent_type())
                .build();

        ContentLog savedLog = contentLogRepository.save(log);

        //파일저장
        FileEntity savedFile = fileService.saveAiImage(savedLog,base64Data,extension);

        String fullImageUrl = fileService.getFullImageUrl(savedFile);

        // 2. 이미지 태그 생성
        String imgTag = String.format(
                "<div style='text-align:center; margin: 20px 0;'>" +
                        "  <img src='%s' style='max-width:100%%; border-radius:15px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);' />" +
                        "  <p style='color:#888; font-size:0.9em;'>[AI가 생성한 이미지입니다]</p>" +
                        "</div>",
                fullImageUrl
        );

        // 3. [IMAGE_HERE] 치환자로 이미지 박아넣기!
        // 파이썬이 준 HTML 본문에서 글자만 슥 바꿔주면 끝납니다.
        String finalHtml = generatedResult.replace("[IMAGE_HERE]", imgTag);

        //저장된 파일과 html 합치기


        // 3. 리액트에게는 AI가 만든 텍스트만 돌려줍니다.
        Map<String, String> result = new HashMap<>();
        result.put("generated_text", finalHtml);
        result.put("generated_title", generatedTitle); // 🚀 리액트 'Final Post Title' 칸에 꽂아줄 제목
        result.put("log_id", String.valueOf(savedLog.getLogId()));
        result.put("content_type", request.getContent_type());

        return result;
    }

    @Transactional
    public ContentResponse finalizeAndSaveContent(ContentSaveRequest request, String userId) {

        // 1. 원본 로그(ContentLog) 조회
        // (JPA 영속성 컨텍스트가 이 객체를 관리하기 시작함)
        Long logId = (request.getLogId() != null) ? Long.valueOf(request.getLogId()) : null;
        ContentLog contentLog = contentLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("원본 로그를 찾을 수 없습니다."));

        // 2. 최종 컨텐츠 엔티티 생성 및 저장
        Content content = Content.builder()
                .projectId(request.getProjectId())
                .logId(contentLog.getLogId())
                .title(request.getTitle())
                .body(request.getBody())
                .contentType(request.getContentType())
                .status("PUBLISHED")
                .build();

        Content savedContent = contentRepository.save(content);

        // 3. [더티 체킹 포인트] 파일 상태 변경
        // 레포지토리에서 가져온 FileEntity들은 모두 '영속 상태'입니다.
        List<FileEntity> files = fileRepository.findByContentLog(contentLog);

        if (files != null && !files.isEmpty()) {
            for (FileEntity file : files) {
                // 사장님이 만드신 메서드 호출!
                // 내부에서 필드 값이 바뀌는 순간, JPA가 "어? 이거 나중에 수정해야겠네" 하고 메모해둡니다.
                file.confirmContent(savedContent);
            }
        }

        // 4. 리턴문 실행 후 메서드가 종료되면서 @Transactional에 의해 커밋(Commit) 발생!
        // 이때 JPA가 바뀐 파일들을 찾아내서 자동으로 UPDATE 쿼리를 날립니다.
        return ContentResponse.fromEntity(savedContent);
    }

    @Transactional(readOnly = true)
    public List<ProjectTreeDTO> makeTreeStructure(String userId) {
        // 1. 해당 유저가 소유한 모든 프로젝트(Master) 목록 조회
        List<Project> projects = projectRepository.findByUserId(Long.valueOf(userId));

        // 2. 프로젝트 리스트를 순회하며 Tree 구조 DTO로 변환
        return projects.stream().map(project -> {

            // 3. 해당 프로젝트에 속한 모든 컨텐츠(Detail) 목록 조회 및 DTO 변환
            List<ContentTreeDTO> contentTreeDTOList = contentRepository.findByProjectId(project.getProjectId())
                    .stream()
                    .map(content -> ContentTreeDTO.builder()
                            .contentId(content.getContentId())
                            .title(content.getTitle())
                            .build())
                    .collect(Collectors.toList());

            // 4. 프로젝트 정보와 컨텐츠 리스트를 결합하여 반환
            return ProjectTreeDTO.builder()
                    .projectId(project.getProjectId())
                    .name(project.getName())
                    .children(contentTreeDTOList) // 여기서 Detail 배열이 합쳐짐
                    .build();

        }).collect(Collectors.toList());
    }

    /**
     * 콘텐츠 상세 정보 조회
     * @param userId 현재 로그인한 사용자 ID (보안 체크용)
     * @param contentId 조회할 콘텐츠 PK
     * @return ContentResponse (제목, HTML 본문 등)
     */
    public ContentResponse getContentDetail(String userId, Long contentId) {
        // 1. DB에서 데이터 조회 (Repository에서 Optional<Content>를 반환한다고 가정)
        Content content = contentRepository.findByContentId(contentId);

        // 2. 결과 검증 (보안 체크: 콘텐츠 소유자 확인 로직이 필요하다면 여기서 수행)
        // 현재 엔티티에 userId가 없다면 projectId 등을 통해 검증 로직을 추가할 수 있습니다.

        // 3. Response DTO로 변환하여 반환 (Builder 패턴 사용)
        return ContentResponse.builder()
                .projectId(content.getProjectId())
                .contentId(content.getContentId())
                .createdAt(content.getCreatedAt())
                .title(content.getTitle())
                .body(content.getBody())      // 엔티티의 body(HTML)를 DTO의 content에 매핑
                .build();
    }


    /**
     * 콘텐츠 삭제
     * @param userId 현재 로그인한 사용자 ID (소유권 검증용)
     * @param contentId 삭제할 콘텐츠 PK
     */
    @Transactional
    public void deleteContent(String userId, Long contentId) {
        // 1. 콘텐츠 존재 여부 및 소유권 확인
        Content content = contentRepository.findByContentId(contentId);

        // 2. [물리 삭제 전] 관련 로깅 처리 (포트폴리오 어필용 Audit Log)
        // 별도의 로그 테이블이 있다면 여기서 save 처리
        log.info("Content Hard Delete Request - User: {}, ContentID: {}, Title: {}",
                userId, contentId, content.getTitle());

        // 3. 실제 파일 삭제 (비용 절감 핵심: S3나 로컬 저장소 파일 제거)
        fileService.deleteFilesByContentId(contentId);


        // 4. DB 물리 삭제 (Hard Delete)
        // 관계 매핑 시 CascadeType.REMOVE가 걸려 있다면 연관된 태그/댓글도 함께 삭제됨
        contentRepository.delete(content);
    }
}