package kopo.gagyeview.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBnkDTO {
    private String userId;

    @NotBlank(message = "은행 이름은 필수 항목입니다.")
    private String bnkNm;


    private BigDecimal iniNum;

    @NotNull(message = "현재 금액은 필수 항목입니다.")
    @DecimalMin(value = "0.00", message = "현재 금액은 0 이상이어야 합니다.")
    private BigDecimal curNum;

    private String regId;
    private LocalDateTime regDt;
    private String chgId;
    private LocalDateTime chgDt;
}
