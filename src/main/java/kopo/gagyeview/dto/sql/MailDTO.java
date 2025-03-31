package kopo.gagyeview.dto.sql;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MailDTO {

    String toMail; // 받는 사람

    String title; // 보내는 메일 제목

    String text; // 보내는 메일 내용
}
