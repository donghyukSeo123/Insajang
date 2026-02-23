package com.project.insajang.user.entity;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class UserPrincipal extends User {
    private final Long id; // 우리가 쓸 숫자 ID

    public UserPrincipal(Long userId) {
        // 부모 클래스 User(아이디, 비밀번호, 권한) 설정
        // 아이디 자리에 userId를 문자열로 넣고, 비번은 빈값으로 둡니다.
        super(String.valueOf(userId), "", Collections.emptyList());
        this.id = userId;
    }
}