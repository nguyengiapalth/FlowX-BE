package project.ii.flowx.applications.service.helper;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import project.ii.flowx.applications.events.UserEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync(proxyTargetClass = true)
public class MailService {

    final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@flowx.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;


    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(UserEvent.UserCreatedEvent event) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");


        ClassPathResource resource = new ClassPathResource("templates/welcome-mail.html");
        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // Replace placeholders in the HTML content
        htmlContent = htmlContent.replace("{{fullname}}", event.fullName())
                                 .replace("{{username}}", event.email())
                                 .replace("{{password}}", event.password())
                                    .replace("{{position}}", event.position() != null ? event.position() : "N/A");

        helper.setTo("nguyengiapnfif@gmail.com");
        helper.setSubject("Welcome to FlowX, " + event.fullName() + "!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("FlowX - Đặt lại mật khẩu");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản FlowX của mình.\n\n" +
                            "Vui lòng nhấp vào liên kết bên dưới để đặt lại mật khẩu:\n" +
                            "%s\n\n" +
                            "Liên kết này sẽ hết hạn sau 15 phút.\n\n" +
                            "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ FlowX",
                    userName, resetUrl
            );

            message.setText(emailContent);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
