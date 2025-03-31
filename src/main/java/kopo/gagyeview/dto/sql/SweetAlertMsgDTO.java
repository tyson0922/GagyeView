package kopo.gagyeview.dto.sql;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SweetAlertMsgDTO {

    private int result; // 성공 : 1 / 실패 : 그 외

    private String icon; // sweet alert icon

    private String title; // sweet alert title

    private String text; // sweet alert text

}
