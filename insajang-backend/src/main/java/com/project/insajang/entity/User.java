package com.project.insajang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 엔티티
 * 페이스북 OAuth2를 통해 수집된 정보와 인스타그램 API 호출을 위한 액세스 토큰을 관리합니다.
 */
@Entity
@Table(name = "users")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;    // 사용자 실명

    private String email;   // 페이스북 등록 이메일

    @Column(length = 1000)
    private String accessToken; // 페이스북 그래프 API 및 인스타그램 비즈니스 API 액세스 토큰

    @Column(unique = true)
    private String facebookId; // 페이스북 사용자 고유 식별자 (Provider ID)

    private String instagramBusinessAccountId; // 연결된 인스타그램 프로페셔널 계정 ID

    private String linkedPageId; // 인스타그램 계정과 연결된 페이스북 페이지 ID

    private LocalDateTime lastSyncedAt; // 마지막 동기화시간

    private LocalDateTime tokenExpiresAt; // 토큰 만료시간


}
