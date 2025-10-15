package kopo.gagyeview.feign;

import kopo.gagyeview.dto.ChatGptRequestDTO;
import kopo.gagyeview.dto.ChatGptResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "chatgpt", url = "https://api.openai.com/v1/completions")
public interface ChatGptFeignClient {
    @PostMapping
    ChatGptResponseDTO getSummary(
        @RequestHeader("Authorization") String authorization,
        @RequestBody ChatGptRequestDTO request
    );
}

