package kopo.gagyeview.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExistsYnDTO {
    private String existsYn;
    private Integer authNumber;
}
