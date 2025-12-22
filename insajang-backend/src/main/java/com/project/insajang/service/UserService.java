package com.project.insajang.service;

import com.project.insajang.entity.User;
import com.project.insajang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스
 * 회원 가입 처리 및 기존 회원의 액세스 토큰 갱신 로직을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * OAuth2 인증 정보를 바탕으로 사용자 정보를 저장하거나 업데이트합니다.
     */
    @Transactional
    public User saveOrUpdate(String facebookId, String name, String email, String accessToken) {
        User user = userRepository.findByFacebookId(facebookId)
                .map(existingUser -> {
                    // 기존 사용자의 경우 성함 및 액세스 토큰 최신화
                    existingUser.setAccessToken(accessToken);
                    existingUser.setName(name);
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 신규 사용자의 경우 정보 생성
                    User newUser = new User();
                    newUser.setFacebookId(facebookId);
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setAccessToken(accessToken);
                    return newUser;
                });
        return userRepository.save(user);
    }
}
