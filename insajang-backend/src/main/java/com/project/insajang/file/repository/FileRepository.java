package com.project.insajang.file.repository;

import com.project.insajang.content.entity.ContentLog;
import com.project.insajang.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByContentLog(ContentLog contentLog);
}