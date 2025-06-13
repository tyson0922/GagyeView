package kopo.gagyeview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefCatDTO {
    private Integer defCatId;
    private String catNm;
    private String catType;
    private String regId;
    private LocalDateTime regDt;
    private String chgId;
    private LocalDateTime chgDt;
}