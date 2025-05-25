package project.ii.flowx.applications.service.helper;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import project.ii.flowx.applications.events.UserEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailService {

    JavaMailSender mailSender;

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

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

}
