package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MartDTO(
        @NotBlank(message = "마트 이름은 필수 학목입니다.") String mName,
        String mAddress,
        String mPhoneNm,
        String mUrl,
        String x,
        String y
) {}
