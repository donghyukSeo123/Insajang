package com.project.insajang.user.controller;

import com.project.insajang.config.JwtTokenProvider;
import com.project.insajang.user.dto.LoginRequest;
import com.project.insajang.user.dto.UserJoinRequest;
import com.project.insajang.user.entity.User;
import com.project.insajang.user.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.project.insajang.user.entity.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/email-verification")
    public ResponseEntity<?> sendEmail(@RequestBody VerificationRequest request) {


        boolean isExists = userService.existsByEmail(request.getEmail());

        if (isExists){
            log.info("사용중인 이메일");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 이메일입니다.");
        }

        userService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/email-confirmation")
    public ResponseEntity<?> verifyCode(@RequestBody VerificationRequest request) {

        log.info("이메일 : " + request.getEmail());
        log.info("코드 : " + request.getCode());

        boolean isVerified = userService.confirmCode(request.getEmail(), request.getCode());

        if (isVerified) {
            return ResponseEntity.ok("인증에 성공했습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 만료되었습니다.");
        }
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody UserJoinRequest request) {
        // 프론트에서 넘어온 13자 검증은 @Valid가 처리해줄 겁니다.
        userService.saveUser(request);
        return ResponseEntity.ok("인사장님, 회원가입을 축하드립니다!");
    }


    /**
     *  닉네임 중복 체크 (디바운싱용)
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        // 서비스에서 existsByNickname(nickname) 호출
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);

        // 중복이면 true, 사용 가능하면 false 반환
        return ResponseEntity.ok(!isDuplicate);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());

        if (user != null) {
            String accessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

            // DB에 Refresh Token 저장/갱신
            userService.saveRefreshToken(user.getId(), refreshToken);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userName", user.getName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    /**
     * 토큰 재발행 API (Silent Reissue)
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("리프레시 토큰이 누락되었습니다.");
        }

        try {
            Map<String, String> tokens = userService.reissueToken(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            log.warn("토큰 재발행 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * 마이페이지 내 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            User user = userService.getUserById(principal.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("name", user.getName());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("emailOnPublish", "Y".equalsIgnoreCase(user.getEmailOnPublish()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 비밀번호 수정 API
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("비밀번호 정보를 모두 입력해주세요.");
        }

        try {
            userService.changePassword(principal.getId(), currentPassword, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            Map<String, String> errorRes = new HashMap<>();
            errorRes.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorRes);
        }
    }

    /**
     * 알림 수신 설정 수정 API
     */
    @PostMapping("/update-settings")
    public ResponseEntity<?> updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Boolean> request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Boolean emailOnPublish = request.get("emailOnPublish");
        if (emailOnPublish == null) {
            return ResponseEntity.badRequest().body("설정 정보가 누락되었습니다.");
        }

        try {
            userService.updateSettings(principal.getId(), emailOnPublish);
            return ResponseEntity.ok("설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class VerificationRequest {
        private String email;
        private String code;
    }
}