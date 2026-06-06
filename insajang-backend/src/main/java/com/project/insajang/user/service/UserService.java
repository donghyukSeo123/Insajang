package com.project.insajang.user.service;

import com.project.insajang.config.JwtTokenProvider;
import com.project.insajang.email.service.EmailService;
import com.project.insajang.user.dto.UserJoinRequest;
import com.project.insajang.user.entity.RefreshToken;
import com.project.insajang.user.entity.User;
import com.project.insajang.user.repository.RefreshTokenRepository;
import com.project.insajang.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    //인증번호와 생성시간을 함께 저장 (Thread-safe ConcurrentHashMap 적용)
    private final Map<String, VerificationData> verificationStorage = new ConcurrentHashMap<>();

    // 유효 시간 설정 (3분 = 180,000 밀리초)
    private static final long EXPIRED_TIME = 3 * 60 * 1000L;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 이메일 인증번호 발송
     */
    /**
     * 이메일 인증번호 발송
     */
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        verificationStorage.put(email, new VerificationData(code, System.currentTimeMillis()));

        // 💡 핵심 분리: 메일 인프라 처리는 EmailService에 위임
        emailService.sendVerificationCodeEmail(email, code);
    }

    public boolean confirmCode(String email, String code) {
        VerificationData info = verificationStorage.get(email);

        // 1. 해당 이메일로 발송된 기록이 있는지 확인
        if (info == null) {
            return false;
        }

        // 2. 시간 만료 확인 (현재 시간 - 생성 시간 > 3분)
        long currentTime = System.currentTimeMillis();
        if (currentTime - info.getCreatedAt() > EXPIRED_TIME) {
            verificationStorage.remove(email); // 만료됐으면 삭제
            return false;
        }

        // 3. 코드 일치 확인
        boolean isMatch = info.getCode().equals(code);

        if (isMatch) {
            verificationStorage.remove(email); // 인증 성공 후 데이터 삭제 (1회용)
            return true;
        }

        return false;

    }


    // UserService 내부
    public void removeExpiredVerificationCodes() {
        long currentTime = System.currentTimeMillis();

        // 맵을 돌면서 만료된 녀석들만 골라 삭제
        verificationStorage.entrySet().removeIf(entry ->
                (currentTime - entry.getValue().getCreatedAt()) > EXPIRED_TIME
        );

        log.info("만료된 인증번호 청소 완료");
    }

    @Transactional
    public void saveUser(UserJoinRequest request) {
        // 1. 닉네임 중복 체크 (2번 기능을 여기서도 활용)
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
        }

        // 2. 비밀번호 암호화 (BCrypt)
        // 리액트에서 온 생비밀번호를 해시값으로 변환하여 password_hash 컬럼에 맞춤
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. DTO -> Entity 변환 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword) // DB 컬럼 password_hash에 매핑
                .name(request.getName())
                .nickname(request.getNickname())
                .role("USER") // 기본 권한 설정
                .emailOnPublish("Y") // 기본 수신 여부 'Y'
                .build();

        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // 2. matches(입력한 비번, DB에 저장된 암호화된 비번) 비교
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return user; // 일치하면 유저 반환
            }
        }
        return null; // 틀리면 null
    }

    /**
     * 닉네임 중복 여부 확인
     * @param nickname 검사할 닉네임
     * @return 중복이면 true, 사용 가능하면 false
     */
    public boolean isNicknameDuplicate(String nickname) {
        // 닉네임이 비어있거나 공백인 경우 예외처리 또는 true 반환 가능
        if (nickname == null || nickname.trim().isEmpty()) {
            return true;
        }
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 리프레시 토큰 저장 및 갱신 (1인 1로그인/중복 기기 세션 차단 또는 덮어쓰기)
     */
    @Transactional
    public void saveRefreshToken(Long userId, String token) {
        refreshTokenRepository.deleteByUserId(userId); // 기존 토큰 파기(Revocation)

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(7)) // 7일 유효
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 리프레시 토큰 검증 및 재발급 (Refresh Token Rotation 적용)
     */
    @Transactional
    public Map<String, String> reissueToken(String refreshTokenStr) {
        // 1. DB에서 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 유효하지 않은 리프레시 토큰입니다."));

        // 2. 만료 여부 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }

        // 3. 서명 유효성 및 만료 검증
        if (!jwtTokenProvider.validateToken(refreshTokenStr)) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료되었거나 서명이 유효하지 않은 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }

        // 4. 유저 정보 조회 및 새로운 이중 토큰 생성
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshTokenStr = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // 5. 토큰 로테이션 적용: 기존 토큰 레코드를 새 토큰으로 업데이트
        refreshToken.setToken(newRefreshTokenStr);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshTokenStr);

        return tokens;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }

    @Transactional
    public void updateSettings(Long userId, boolean emailOnPublish) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String val = emailOnPublish ? "Y" : "N";
        user.updateEmailOnPublish(val);
        userRepository.save(user);
    }

    @Getter
    @AllArgsConstructor
    public static class VerificationData {
        private String code;
        private long createdAt;
    }
}