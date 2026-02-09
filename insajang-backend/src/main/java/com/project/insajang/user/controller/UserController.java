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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/login")
        public ResponseEntity<?> checkNickname(@Valid @RequestBody LoginRequest request) {
            // 서비스에서 existsByNickname(nickname) 호출
            User user = userService.login(request.getEmail(),request.getPassword());

        if (user != null) {
            String token = jwtTokenProvider.createToken(user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);
            response.put("userName", user.getName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
    @Getter
    @Setter
    public static class VerificationRequest {
        private String email;
        private String code;
    }
}