package kopo.gagyeview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequestDTO {
    private String prompt;
    private int maxTokens;
    private String model;
}
