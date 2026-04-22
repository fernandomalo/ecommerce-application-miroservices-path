package com.fernando.microservices.auth_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendVerificationEmail(String to, Integer verificationCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Verify your account");
        helper.setText(buildHtml(verificationCode), true);

        javaMailSender.send(message);
    }

    private String buildHtml(Integer verificationCode) {
        String code = String.format("%06d", verificationCode);
        String d1 = String.valueOf(code.charAt(0));
        String d2 = String.valueOf(code.charAt(1));
        String d3 = String.valueOf(code.charAt(2));
        String d4 = String.valueOf(code.charAt(3));
        String d5 = String.valueOf(code.charAt(4));
        String d6 = String.valueOf(code.charAt(5));

        String digitBox = "<td style=\"padding:0 5px;\"><div style=\"width:48px;height:60px;"
                + "background:#f5f5f5;border:1px solid #ddd;border-radius:8px;"
                + "text-align:center;line-height:60px;font-size:28px;font-weight:600;"
                + "color:#111;font-family:monospace;\">{{DIGIT}}</div></td>";

        return """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head><meta charset="UTF-8"/></head>
                    <body style="margin:0;padding:0;background:#f0f0f0;font-family:'Segoe UI',Arial,sans-serif;">
                      <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
                        <tr><td align="center">
                          <table width="600" cellpadding="0" cellspacing="0"
                                 style="background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e0e0e0;">

                            <!-- Header -->
                            <tr>
                              <td style="background:#111;padding:28px 40px;">
                                <table width="100%" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td>
                                      <table cellpadding="0" cellspacing="0">
                                        <tr>
                                          <img src="https://res.cloudinary.com/dvouyq8xq/image/upload/v1774998208/c7f4b3a6-9443-43ae-9f94-72ee59645849_1_ypbhc7.png" alt="Ecomod logo" style="width:80px;height:80px;vertical-align:middle;"/>
                                          <td style="padding-left:12px;color:#fff;font-size:18px;font-weight:600;">Ecomod</td>
                                        </tr>
                                      </table>
                                    </td>
                                    <td align="right" style="color:#666;font-size:12px;">Account security</td>
                                  </tr>
                                </table>
                              </td>
                            </tr>

                            <!-- Accent line -->
                            <tr><td style="height:3px;background:#333;"></td></tr>

                            <!-- Title -->
                            <tr>
                              <td style="padding:40px 48px 24px;">
                                <p style="margin:0 0 6px;font-size:11px;letter-spacing:1.5px;color:#888;text-transform:uppercase;">Account security</p>
                                <h1 style="margin:0 0 16px;font-size:24px;font-weight:600;color:#111;">Verify your email address</h1>
                                <p style="margin:0;font-size:15px;color:#555;line-height:1.7;">
                                  Hi there — thanks for signing up. Enter the code below to confirm your
                                  email and activate your account. This code expires in <strong style="color:#111;">5 minutes</strong>.
                                </p>
                              </td>
                            </tr>

                            <!-- Code boxes -->
                            <tr>
                              <td style="padding:0 48px 36px;">
                                <div style="border:1px solid #e0e0e0;border-radius:12px;padding:28px 32px;text-align:center;">
                                  <p style="margin:0 0 20px;font-size:11px;letter-spacing:1.2px;color:#888;text-transform:uppercase;">Your verification code</p>
                                  <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                                    <tr>
                                      {{D1}}{{D2}}{{D3}}
                                      <td style="padding:0 8px;font-size:20px;color:#ccc;vertical-align:middle;">—</td>
                                      {{D4}}{{D5}}{{D6}}
                                    </tr>
                                  </table>
                                </div>
                              </td>
                            </tr>

                            <!-- Info pills -->
                            <tr>
                              <td style="padding:0 48px 36px;border-bottom:1px solid #f0f0f0;">
                                <table width="100%" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td style="padding:16px;background:#f8f8f8;border-radius:8px;width:33%;">
                                      <p style="margin:0 0 4px;font-size:10px;letter-spacing:1px;color:#888;text-transform:uppercase;">Expires in</p>
                                      <p style="margin:0;font-size:14px;font-weight:600;color:#111;">5 minutes</p>
                                    </td>
                                    <td width="12"></td>
                                    <td style="padding:16px;background:#f8f8f8;border-radius:8px;width:33%;">
                                      <p style="margin:0 0 4px;font-size:10px;letter-spacing:1px;color:#888;text-transform:uppercase;">Single use</p>
                                      <p style="margin:0;font-size:14px;font-weight:600;color:#111;">One-time only</p>
                                    </td>
                                    <td width="12"></td>
                                    <td style="padding:16px;background:#f8f8f8;border-radius:8px;width:33%;">
                                      <p style="margin:0 0 4px;font-size:10px;letter-spacing:1px;color:#888;text-transform:uppercase;">Secure</p>
                                      <p style="margin:0;font-size:14px;font-weight:600;color:#111;">Encrypted</p>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>

                            <!-- Disclaimer -->
                            <tr>
                              <td style="padding:24px 48px;border-bottom:1px solid #f0f0f0;">
                                <p style="margin:0;font-size:13px;color:#888;line-height:1.7;">
                                  Didn't request this? You can safely ignore this email — your account won't be
                                  activated without the code. Concerned? Contact us at
                                  <span style="color:#111;">support@ecomod.co</span>
                                </p>
                              </td>
                            </tr>

                            <!-- Footer -->
                            <tr>
                              <td style="padding:20px 48px;">
                                <table width="100%" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td style="font-size:12px;color:#aaa;">© 2025 Ecomod. All rights reserved.</td>
                                    <td align="right">
                                      <span style="font-size:12px;color:#aaa;padding-left:16px;">Privacy</span>
                                      <span style="font-size:12px;color:#aaa;padding-left:16px;">Terms</span>
                                      <span style="font-size:12px;color:#aaa;padding-left:16px;">Unsubscribe</span>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>

                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                """
                .replace("{{D1}}", digitBox.replace("{{DIGIT}}", d1))
                .replace("{{D2}}", digitBox.replace("{{DIGIT}}", d2))
                .replace("{{D3}}", digitBox.replace("{{DIGIT}}", d3))
                .replace("{{D4}}", digitBox.replace("{{DIGIT}}", d4))
                .replace("{{D5}}", digitBox.replace("{{DIGIT}}", d5))
                .replace("{{D6}}", digitBox.replace("{{DIGIT}}", d6));
    }
}
