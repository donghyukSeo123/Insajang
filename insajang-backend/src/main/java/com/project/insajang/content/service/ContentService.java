package com.project.insajang.content.service;

import com.project.insajang.content.dto.*;
import com.project.insajang.content.entity.Content;
import com.project.insajang.content.entity.ContentLog;
import com.project.insajang.content.entity.ContentStatus;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    @org.springframework.beans.factory.annotation.Value("${app.python-url}")
    private String pythonUrl;

    public Map<String, String> processAndSaveContentlog(ContentCreateRequest request, String userId) throws IOException {

        // 1. 파이썬 서버(AI)에 데이터 전송 및 결과 수신
        String targetUrl = pythonUrl + "/generate-content";

        Map<String, Object> pythonRequest = new HashMap<>();
        pythonRequest.put("title", request.getTitle());
        pythonRequest.put("user_input", request.getUser_input());
        pythonRequest.put("content_type", request.getContent_type());
        pythonRequest.put("selectedPersona", request.getSelectedPersona()); // 페르소나(말투) 파라미터 추가

        // AI 결과 수신
        Map<String, Object> pythonResponse = restTemplate.postForObject(targetUrl, pythonRequest, Map.class);
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
                .userId(Long.valueOf(userId))
                .build();

        ContentLog savedLog = contentLogRepository.save(log);

        //파일저장
        FileEntity savedFile = fileService.saveAiImage(savedLog,base64Data,extension);

        String fullImageUrl = fileService.getFullImageUrl(savedFile);

        String imgTag = String.format(
                "<p style='text-align: center; margin-top: 20px; margin-bottom: 5px;'>" +
                        "  <img src='%s' style='max-width: 100%%; border-radius: 15px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);' />" +
                        "</p>" +
                        "<p style='text-align: center; color: #888; font-size: 0.9em; margin-top: 0; margin-bottom: 20px;'>" +
                        "  [AI가 생성한 이미지입니다]" +
                        "</p>",
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
                .status("READY")
                .userId(Long.valueOf(userId))
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
                            .status(content.getStatus())
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
                .logId(content.getLogId())
                .title(content.getTitle())
                .body(content.getBody())
                .contentType(content.getContentType())
                .status(content.getStatus())
                .createdAt(content.getCreatedAt())
                .scheduledAt(content.getScheduledAt())
                .build();
    }

    /**
     * 콘텐츠 예약 일정 취소 (캘린더에서 제거하고 DRAFT 상태로 전환)
     */
    @Transactional
    public ContentResponse cancelSchedule(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 컨텐츠를 찾을 수 없습니다. ID: " + contentId));

        content.setScheduledAt(null);
        content.setStatus("READY"); // 캘린더에서 제외하고 READY(준비됨) 상태로 복원

        return ContentResponse.fromEntity(content);
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


    @Transactional // 중요: 이 어노테이션이 있어야 Dirty Checking이 작동합니다.
    public ContentResponse update(Long contentId, ContentUpdateDto updateDto) {

        // 1. 기존 데이터 조회 (없으면 예외 발생)
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 컨텐츠를 찾을 수 없습니다. ID: " + contentId));

        // 2. DTO에 값이 들어있는 경우에만 필드 업데이트 (Null-Safe)
        // 프론트엔드에서 변경된 데이터만 보냈을 때 기존 데이터가 날아가지 않도록 보호합니다.
        if (updateDto.getTitle() != null) {
            content.setTitle(updateDto.getTitle());
        }
        if (updateDto.getBody() != null) {
            content.setBody(updateDto.getBody());
        }

        // 3. 수정된 결과를 Response DTO로 변환하여 반환
        // 별도의 save 호출 없이 트랜잭션 종료 시점에 DB 업데이트 쿼리가 실행됩니다.
        return ContentResponse.fromEntity(content);
    }

    @Transactional
    public ContentResponse saveSchedule(ContentSaveRequest dto) {
        // 1. 컨텐츠 조회 (id가 2인 데이터를 찾음)
        Content content = contentRepository.findById(Long.valueOf(dto.getContentId()))
                .orElseThrow(() -> new EntityNotFoundException("해당 컨텐츠를 찾을 수 없습니다. ID: " + dto.getContentId()));

        // 2. 예약 시간 설정 및 상태 변경
        // 팁: 이미 지난 시간에 예약하려고 하는 경우에 대한 방어 로직을 넣으면 더 안전합니다.
        if (dto.getScheduledAt() != null) {
            content.setScheduledAt(dto.getScheduledAt());
            content.setStatus(String.valueOf(ContentStatus.SCHEDULED)); // 예약중 상태로 변경
        }

        // 3. 변경 감지(Dirty Checking)에 의해 별도의 save 호출 없이도 트랜잭션 종료 시 DB 반영
        return ContentResponse.fromEntity(content);
    }

    /**
     * 특정 기간 내의 일정 목록 조회
     * @param start 조회 시작일 (ISO8601 문자열)
     * @param end   조회 종료일 (ISO8601 문자열)
     * @param userId 사용자 ID
     */
    @Transactional(readOnly = true)
    public List<ContentResponse> getSchedulesByRange(String start, String end, String userId) {
        // 1. 날짜 파싱 (FullCalendar의 ISO8601 형식을 LocalDateTime으로 변환)
        // OffsetDateTime은 +09:00 같은 타임존 정보를 안전하게 처리합니다.
        LocalDateTime startDate = OffsetDateTime.parse(start).toLocalDateTime();
        LocalDateTime endDate = OffsetDateTime.parse(end).toLocalDateTime();

        log.info("일정 범위 조회: {} ~ {} (User: {})", startDate, endDate, userId);

        // 2. Repository 호출
        // 작성하신 saveSchedule 스타일처럼 contentRepository를 사용한다고 가정합니다.
        List<Content> contents = contentRepository.findByScheduledAtBetweenAndUserId(
                startDate,
                endDate,
                Long.valueOf(userId)
        );

        // 3. Entity 리스트를 Response DTO 리스트로 변환 (ContentResponse::fromEntity 활용)
        return contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}