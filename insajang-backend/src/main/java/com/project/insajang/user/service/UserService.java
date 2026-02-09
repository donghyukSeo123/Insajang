package com.project.insajang.user.service;

import com.project.insajang.user.dto.UserJoinRequest;
import com.project.insajang.user.entity.User;
import com.project.insajang.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //인증번호와 생성시간을 함께 저장
    private final Map<String, VerificationData> verificationStorage = new HashMap<>();

    // 유효 시간 설정 (3분 = 180,000 밀리초)
    private static final long EXPIRED_TIME = 3 * 60 * 1000L;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 이메일 인증번호 발송
     */
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        verificationStorage.put(email, new VerificationData(code, System.currentTimeMillis()));

        // 1. MimeMessage 생성
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 2. MimeMessageHelper를 사용 (true는 멀티파트 메시지를 의미)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[컨텐츠메이커스튜디오] 회원가입 인증번호 안내");

            // 3. HTML 본문 작성 (컨텐츠메이커스튜디오 스타일)
            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;'>" +
                            "  <div style='background-color: #007bff; color: white; padding: 20px; text-align: center;'>" +
                            "    <h2 style='margin: 0;'>CONTENTS MAKER STUDIO</h2>" +
                            "  </div>" +
                            "  <div style='padding: 30px; line-height: 1.6; color: #333;'>" +
                            "    <p>안녕하세요, <strong>컨텐츠메이커스튜디오</strong>입니다.</p>" +
                            "    <p>서비스 이용을 위해 아래의 인증번호를 입력해 주세요.</p>" +
                            "    <div style='background-color: #f8f9fa; border: 1px dashed #007bff; padding: 20px; text-align: center; margin: 20px 0;'>" +
                            "      <span style='font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>" + code + "</span>" +
                            "    </div>" +
                            "    <p style='font-size: 14px; color: #666;'>* 본 인증번호는 <strong>3분 이내</strong>에 입력하셔야 합니다.<br>" +
                            "    * 요청하신 적이 없다면 본 메일을 무시해 주세요.</p>" +
                            "  </div>" +
                            "  <div style='background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #888;'>" +
                            "    © 2026 CONTENTS MAKER STUDIO. All rights reserved." +
                            "  </div>" +
                            "</div>";

            helper.setText(htmlContent, true); // true를 넣어야 HTML로 렌더링됩니다.
            helper.setFrom(fromEmail, "컨텐츠메이커스튜디오");

            mailSender.send(message);
            System.out.println("HTML 메일 발송 완료! 인증번호: " + code);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("메일 발송 실패: " + e.getMessage());
        }
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

    @Getter
    @AllArgsConstructor
    public static class VerificationData {
        private String code;
        private long createdAt;
    }
}