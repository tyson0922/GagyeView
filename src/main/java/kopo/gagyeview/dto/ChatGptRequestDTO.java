package kopo.gagyeview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequestDTO {
    private String model;
    @JsonProperty("messages")
    private List<Message> messages;
    @JsonProperty("max_completion_tokens")
    private int max_completion_tokens;
    @JsonProperty("temperature")
    private Double temperature;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
