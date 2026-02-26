package com.project.insajang.project.service;

import com.project.insajang.project.dto.ProjectCreateDto;
import com.project.insajang.project.dto.ProjectResponseDto;
import com.project.insajang.project.entity.Project;
import com.project.insajang.project.repository.ProjectRepository;
import com.project.insajang.user.entity.User;
import com.project.insajang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository; // 유저를 찾아오기 위해 필요합니다.

    @Transactional
    public ProjectResponseDto saveProject(ProjectCreateDto createDto, Long userId) {
        // 1. 유저 ID로 실제 유저 엔티티를 찾습니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. DTO -> Entity 변환 (생성자나 빌더 사용)
        Project project = Project.builder()
                .name(createDto.getName())
                .user(user) // 주인(인사장님)을 설정해줍니다.
                .build();

        // 3. DB 저장
        Project savedProject = projectRepository.save(project);

        // 4. 저장된 결과를 ResponseDto로 변환해서 반환
        return ProjectResponseDto.from(savedProject);
    }

    public List<ProjectResponseDto> getUserProjects(String userId) {
        // 1. Repository를 통해 특정 유저의 프로젝트 엔티티 리스트 조회
        // userId가 String인 경우, Repository에서도 String으로 조회 조건을 맞춥니다.
        List<Project> projects = projectRepository.findByUser_IdOrderByCreatedAtDesc(Long.valueOf(userId));

        // 2. Entity 리스트를 Stream을 사용하여 DTO 리스트로 변환
        return projects.stream()
                .map(ProjectResponseDto::from) // ProjectResponseDto.from(project)를 호출
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 삭제 로직
     * @param userId 현재 로그인한 유저의 PK (Long)
     * @param projectId 삭제할 프로젝트의 PK (Long)
     */
    @Transactional // 데이터 삭제 작업이므로 트랜잭션 처리가 필수
    public void deleteProject(Long userId, Long projectId) {

        // 1. 해당 프로젝트가 있는지, 그리고 주인이 현재 요청한 유저가 맞는지 확인
        // 이 메서드는 Repository에 추가해야 합니다 (아래 참고)
        Project project = projectRepository.findByProjectIdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없거나 삭제 권한이 없습니다. ID: " + projectId));

        // 2. 검증이 끝났다면 삭제 실행
        projectRepository.delete(project);
    }
}