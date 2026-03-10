package com.project.insajang.content.repository;

import com.project.insajang.content.entity.ContentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentLogRepository extends JpaRepository<ContentLog, Long> {

}