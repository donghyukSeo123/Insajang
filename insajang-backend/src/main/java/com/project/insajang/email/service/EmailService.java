package com.project.insajang.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 회원가입 인증번호 메일 발송
     */
    public void sendVerificationCodeEmail(String email, String code) {
        String subject = "[컨텐츠메이커스튜디오] 회원가입 인증번호 안내";
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

        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * 콘텐츠 예약 게시 알림 링크 발송
     */
    public void sendPublishLinkEmail(String email,String contentTitle, String publishLink) {
        String subject = "[컨텐츠메이커스튜디오] 예약하신 컨텐츠가 준비되었습니다.";
        String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;'>" +
                        "  <div style='background-color: #007bff; color: white; padding: 20px; text-align: center;'>" +
                        "    <h2 style='margin: 0;'>CONTENTS MAKER STUDIO</h2>" +
                        "  </div>" +
                        "  <div style='padding: 30px; line-height: 1.6; color: #333;'>" +
                        "    <p>안녕하세요, <strong>컨텐츠메이커스튜디오</strong>입니다.</p>" +
                        "    <p>제작 완료된 콘텐츠(<strong>" + contentTitle + "</strong>)의 배포 준비가 모두 끝났습니다.</p>" +
                        "    <p>아래 버튼을 클릭하시면 최종 완성된 이미지와 텍스트를 확인하고, 원하시는 소셜 미디어 플랫폼에 바로 발행하실 수 있습니다.</p>" +
                        "    <div style='text-align: center; margin: 30px 0;'>" +
                        "      <a href='" + publishLink + "' style='background-color: #007bff; color: white; padding: 15px 30px; text-align: center; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; display: inline-block; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +
                        "        콘텐츠 확인 및 발행하기" +
                        "      </a>" +
                        "    </div>" +
                        "    <p style='font-size: 14px; color: #666;'>* 본 확인 링크는 보안을 위해 <strong>발송 후 24시간 동안만</strong> 유효합니다.<br>" +
                        "    * 만약 버튼이 클릭되지 않는다면 아래 주소를 주소창에 복사해 붙여넣어 주세요.<br>" +
                        "    <span style='color: #007bff; word-break: break-all; font-size: 12px;'>" + publishLink + "</span></p>" +
                        "  </div>" +
                        "  <div style='background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #888;'>" +
                        "    © 2026 CONTENTS MAKER STUDIO. All rights reserved." +
                        "  </div>" +
                        "</div>";

        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * 공통 HTML 메일 발송 로직 (내부용 private 메서드로 중복 제거)
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail, "컨텐츠메이커스튜디오");

            mailSender.send(message);
            log.info("HTML 메일 발송 성공! 수신자: {}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("메일 발송 실패: " + e.getMessage());
        }
    }
}