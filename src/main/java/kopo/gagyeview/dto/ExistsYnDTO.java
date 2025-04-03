package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExistsYnDTO( // 존재 여부 확인 dto
        String existsYn
) {
}
