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
}