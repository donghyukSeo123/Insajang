package com.project.insajang.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // DB 테이블 이름을 'users'로 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 (JPA 필수)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 13) //  13자 제한!
    private String name;

    @Column(nullable = false, unique = true, length = 13) // 닉네임도 13자!
    private String nickname;

    @Column(nullable = false)
    private String role; // 예: "USER", "ADMIN"

    // [참고] 생성일, 수정일 등은 나중에 BaseEntity로 뺄 수 있습니다.
}