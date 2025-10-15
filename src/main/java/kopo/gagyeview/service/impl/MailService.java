package kopo.gagyeview.service.impl;

import jakarta.mail.internet.MimeMessage;
import kopo.gagyeview.dto.MailDTO;
import kopo.gagyeview.service.IMailService;
import kopo.gagyeview.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    // 보내는 사람 메일
    @Value("${spring.mail.username}")
    private String fromMail;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Override
    public int sendMail(MailDTO pDTO) {

        log.info("{}.sendMail Start!",this.getClass().getName());

        // 메일 발송 성공여부(발송성공: 1, 발송실패: 0)
        int res = 1;

        // DTO 객체가 메모리에 올라가지 않아  Null이 발생할 수 있기 때문에 if문 사용해서 에러 방지
        if (pDTO == null){
            pDTO = MailDTO.builder().build();
        }

        String toMail = CmmUtil.nvl(pDTO.toMail()); // 받는 사람
        String title = CmmUtil.nvl(pDTO.title()); // 메일 제목
        String contents = CmmUtil.nvl(pDTO.text()); // 메일 내용

        log.info("toMail: {}, title: {}, text: {}", toMail, title, contents);
        log.info("Mail sender username: {}", fromMail);
        log.info("Mail sender password (from @Value): {}", mailPassword);

        // 메일 발송 메시지 구조(파일 첨부 가능)
        MimeMessage message = mailSender.createMimeMessage();

        // 메일 발송 구조를 쉽게 생성하게 도와주는 객체
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");

        try {

            messageHelper.setTo(toMail);
            messageHelper.setFrom(fromMail);
            messageHelper.setSubject(title);
            messageHelper.setText(contents);

            mailSender.send(message);

        } catch (Exception e){

            res = 0;
            log.info("Mail send failed: ", e);
        }

        return res;
    }
}
