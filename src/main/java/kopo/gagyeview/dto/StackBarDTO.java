package kopo.gagyeview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StackBarDTO {
    private String yrMon;    // 연월 (yyyy-MM)
    private String catType;  // 수입 또는 지출
    private String catNm;    // 카테고리 이름
    private Double total;    // 카테고리별 합계
}
