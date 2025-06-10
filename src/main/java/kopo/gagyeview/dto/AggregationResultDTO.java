package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record AggregationResultDTO(
        String yrMon,
        String catType,       // 수입 or 지출
        String catNm,         // 카테고리 이름 (스택 바 차트용)
        Double total,         // 총액 (도넛, 스택 바, 바 차트 공통)
        Double income,        // 수입합 (수입 vs 지출 비교용)
        Double expense        // 지출합 (수입 vs 지출 비교용)
) {}
