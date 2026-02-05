package com.project.insajang.user.repository;

import com.project.insajang.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 닉네임이 존재하는지 확인
    boolean existsByNickname(String nickname);

    //이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);
}