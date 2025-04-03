package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SweetAlertMsgDTO (

        int result, // 성공 : 1 / 실패 : 그 외

        String icon, // sweet alert icon

        String title, // sweet alert title

        String text // sweet alert text

){

    public static SweetAlertMsgDTO success(String title, String text){
        return SweetAlertMsgDTO.builder()
                .result(1)
                .icon("success")
                .title(title)
                .text(text)
                .build();
    }

    public static SweetAlertMsgDTO fail(String title, String text){
        return SweetAlertMsgDTO.builder()
                .result(0)
                .icon("error")
                .title(title)
                .text(text)
                .build();
    }

}
