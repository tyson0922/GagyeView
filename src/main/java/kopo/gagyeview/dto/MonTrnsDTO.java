package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder
@Document(collection = "MON_TRNS")
public record MonTrnsDTO(
        @Id String id,

        String userId, // injected in controller/service
        String yrMon,  // can be derived from trnsDt

        @NotBlank(message = "거래 유형은 필수입니다.")
        String catType, // 거래 유형(지출 / 수입)

        @Valid
        MonTrnsDetailDTO monTrnsDetailDTO, // 거래 상세정보

        Date regDt,
        Date chgDt


) {

        public String getDerivedYrMon() {
                if (monTrnsDetailDTO != null && monTrnsDetailDTO.trnsDt() != null) {
                        return new java.text.SimpleDateFormat("yyyy-MM").format(monTrnsDetailDTO.trnsDt());
                }
                return yrMon; // fallback
        }

}