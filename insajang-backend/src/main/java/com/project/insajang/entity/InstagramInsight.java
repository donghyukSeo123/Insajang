package com.project.insajang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "instagram_insights")
@Getter @Setter
@NoArgsConstructor
public class InstagramInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용량 데이터 (인사이트)
    private Integer reach;          // 도달수
    private Integer impressions;    // 조회수
    private Integer profileViews;   // 프로필 방문수
    private Integer followerCount;  // 팔로워 수

    private LocalDateTime recordedAt; // 데이터 수집 시점

    // 사장님의 User 엔티티와 조인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB상에 user_id라는 컬럼으로 User의 id가 저장
    private User user;
}