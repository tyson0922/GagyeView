package kopo.gagyeview.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder
public record MonTrnsDetailDTO(
        String catNm, // 카테고리명

        @NotNull(message = "거래 날짜는 필수입니다.")
        Date trnsDt, // 거래 날짜

        @NotNull(message = "총금액은 필수입니다.")
        BigDecimal totNm, // 총금액

        String srcNm, // 출처
        String rcptId, // 영수증 아이디
        String note // 메모
) {}