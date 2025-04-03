package kopo.gagyeview.service;

import kopo.gagyeview.dto.MailDTO;

public interface IMailService {

    // 메일 발송
    int sendMail(MailDTO pDTO);
}
