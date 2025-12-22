package com.project.insajang.repository;

import com.project.insajang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 정보 레포지토리
 * Spring Data JPA를 사용하여 기본적인 CRUD 및 사용자 정의 쿼리를 수행합니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 페이스북 고유 ID를 기반으로 사용자 존재 여부를 확인합니다.
     */
    Optional<User> findByFacebookId(String facebookId);
}