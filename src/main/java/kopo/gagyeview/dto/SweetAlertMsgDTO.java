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
}
