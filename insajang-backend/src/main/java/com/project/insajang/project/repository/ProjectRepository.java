package com.project.insajang.project.repository;

import com.project.insajang.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 나중에 "내 프로젝트 목록"만 보고 싶을 때를 대비해
    // 유저 ID로 프로젝트 리스트를 찾는 메서드를 미리 정의해둡니다.
    List<Project> findAllByUserId(Long userId);
}