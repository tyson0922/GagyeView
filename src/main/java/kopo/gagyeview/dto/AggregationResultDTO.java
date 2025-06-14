package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record AggregationResultDTO(
        String name,         // 💡 For donut chart
        Double value,        // 💡 For donut chart

        String yrMon,
        String catType,      // 수입 or 지출
        String catNm,        // For stacked bar
        Double total,        // For stacked bar
        Double income,       // For bar chart
        Double expense       // For bar chart
) {}

