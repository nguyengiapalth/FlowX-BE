package project.ii.flowx.applications.service.helper;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailSender {

    JavaMailSender mailSender;

//    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlContent, true); // true = HTML
//
//        mailSender.send(message);
//    }
//
//    public void sendWellcomeEmail(String to, String subject, String htmlContent) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlContent, true); // true = HTML
//
//        mailSender.send(message);
//    }

}
