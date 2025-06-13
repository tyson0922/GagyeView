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
public class UserCatDTO {
    private Integer userCatId;       // ✅ NEW: PK (AUTO_INCREMENT)
    private String userId;
    private String catNm;
    private String catType;
    private String regId;
    private LocalDateTime regDt;
    private String chgId;
    private LocalDateTime chgDt;

    private String originalCatNm;
    private String originalCatType;
}