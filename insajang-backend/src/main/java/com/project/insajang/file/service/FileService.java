package com.project.insajang.file.service;

import com.project.insajang.content.entity.ContentLog;
import com.project.insajang.file.entity.FileEntity;
import com.project.insajang.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Transactional
    public FileEntity saveAiImage(ContentLog contentLog, String base64Data, String extension) throws IOException {
        // 1. 폴더 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. 파일명 생성 (UUID)
        String savedFileName = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
        Path filePath = uploadPath.resolve(savedFileName);

        // 3. Base64 디코딩 및 물리 저장
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",")[1];
        }
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        Files.write(filePath, decodedBytes);

        log.info("물리 파일 저장 완료: {}", filePath.toAbsolutePath());

        // 4. DB 정보 빌드 및 저장
        FileEntity fileEntity = FileEntity.builder()
                .contentLog(contentLog)
                .savedName(savedFileName)
                .filePath("/" + uploadDir + "/") // 웹 접근 경로용
                .extension(extension)
                .fileSize((long) decodedBytes.length)
                .status("TEMPORARY")
                .build();

        return fileRepository.save(fileEntity);
    }

    // 리액트에게 던져줄 '완성형 URL' 생성 메서드
    public String getFullImageUrl(FileEntity fileEntity) {
        return backendUrl + fileEntity.getFilePath() + fileEntity.getSavedName();
    }

    @Transactional
    public void deleteFilesByContentId(Long contentId) {
        // 1. 해당 콘텐츠에 속한 파일 리스트 조회
        List<FileEntity> files = fileRepository.findByContentContentId(contentId);

        for (FileEntity file : files) {
            try {
                // 2. 물리적 경로 계산 (저장된 uploadDir + UUID 파일명)
                // 주의: DB의 filePath가 "/upload/" 형태라면 실제 OS 경로와 맞춰야 합니다.
                Path filePath = Paths.get(uploadDir).resolve(file.getSavedName());

                // 3. 실제 물리 파일 삭제
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("물리 파일 삭제 성공: {}", filePath.toAbsolutePath());
                } else {
                    log.warn("삭제할 물리 파일이 존재하지 않습니다: {}", filePath.toAbsolutePath());
                }

                // 4. DB 레코드 삭제 (물리 삭제)
                fileRepository.delete(file);

            } catch (IOException e) {
                log.error("파일 삭제 중 오류 발생: {}", file.getSavedName(), e);
                // 필요에 따라 예외를 던지거나, 로그만 남기고 다음 파일로 진행
            }
        }
    }


}