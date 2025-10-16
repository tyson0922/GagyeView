package kopo.gagyeview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarChartDTO {
    private String name;     // 월 (yyyy-MM 형식)
    private Double income;   // 수입 합계
    private Double expense;  // 지출 합계

    public String getMonth() {
        return name;
    }
}