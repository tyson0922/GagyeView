package kopo.gagyeview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonutChartDTO {
    private String name;     // 카테고리 이름
    private Double value;    // 총합 (수입 또는 지출)
}