package com.project.insajang.project.repository;

import com.project.insajang.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Project> findByProjectIdAndUser_Id(Long projectId, Long userId);

    List<Project> findByUserId(Long userId);
}