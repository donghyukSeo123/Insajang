package com.project.insajang.user.controller;

import com.project.insajang.user.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

//    @PostMapping("/email-confirmation")
//    public ResponseEntity<?> verifyCode(@RequestBody VerificationRequest request) {
//        boolean isVerified = userService.confirmCode(request.getEmail(), request.getCode());
//
//        if (isVerified) {
//            return ResponseEntity.ok("인증에 성공했습니다.");
//        } else {
//            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 만료되었습니다.");
//        }
//    }


    @Getter
    @Setter
    public static class VerificationRequest {
        private String email;
        private String code;
    }
}