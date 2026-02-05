package com.project.insajang.user.controller;

import com.project.insajang.user.dto.UserJoinRequest;
import com.project.insajang.user.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/email-verification")
    public ResponseEntity<?> sendEmail(@RequestBody VerificationRequest request) {
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
//    @GetMapping("/check-nickname")
//    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
//        // 서비스에서 existsByNickname(nickname) 호출
//        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
//
//        // 중복이면 true, 사용 가능하면 false 반환
//        return ResponseEntity.ok(isDuplicate);
//    }


    @Getter
    @Setter
    public static class VerificationRequest {
        private String email;
        private String code;
    }
}