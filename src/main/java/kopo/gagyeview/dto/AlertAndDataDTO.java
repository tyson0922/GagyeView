package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlertAndDataDTO<T>( // 데이터, 알럿트 다 담는 dto

        SweetAlertMsgDTO alert, // 사용자에게 알림
        T data // 필요한 데이터
) {
}
