package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.gagyeview.dto.MailDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.service.IMailService;
import kopo.gagyeview.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequestMapping(value="/mail")
@RequiredArgsConstructor
@Controller
public class MailController {

    private final IMailService mailService; // 메일 발송을 위한 서비스 사용하기


    @GetMapping(value="mailForm")
    public String mailForm() {

        log.info("{}.mailForm Start!", this.getClass().getName());

        return "mail/mailForm";
    }
    @ResponseBody
    @PostMapping(value="sendMail")
    public SweetAlertMsgDTO sendMail(HttpServletRequest request) {

        log.info("{}.sendMail Start!", this.getClass().getName());

        String icon;
        String alertTitle;
        String alertContent;

        // 웹 URL로 부터 전달 받는 값들
        String toMail = CmmUtil.nvl(request.getParameter("toMail"));// 받는 사람
        String mailTitle = CmmUtil.nvl(request.getParameter("title"));
        String mailContent = CmmUtil.nvl(request.getParameter("content"));

        // 메일 발송할 정보 넣기 위한 DTO 객체 생성하고  웹에서 받은 값을 DTO에 넣기
        MailDTO pDTO = MailDTO.builder()
                .toMail(toMail)
                .title(mailTitle)
                .text(mailContent)
                .build();

        int res = mailService.sendMail(pDTO);

        if (res == 1) {
            icon = "success";
            alertTitle = "메일 발송 성공";
            alertContent = "메일이 성공적으로 보내졌습니다. 메일함을 확인해주세요.";
        } else {

            icon = "error";
            alertTitle = "메일 발송 실패";
            alertContent = "메일 발송이 실패하였습니다.";

        }
        log.info("icon: {}, alertTitle: {}, alertContent: {}", icon, alertTitle, alertContent);

        SweetAlertMsgDTO rDTO = SweetAlertMsgDTO.builder()
                .result(res)
                .icon(icon)
                .title(alertTitle)
                .text(alertContent)
                .build();


        log.info("{}.sendMail End!", this.getClass().getName());

        return rDTO;

    }

}
