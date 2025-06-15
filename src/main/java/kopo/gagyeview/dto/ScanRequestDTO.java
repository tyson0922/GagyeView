package kopo.gagyeview.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScanRequestDTO {

    // ✅ Base64 이미지 (for Google Vision)
    private String image;

    // ✅ OCR 결과 텍스트 (to send to ChatGPT)
    private String ocrText;

    // ✅ 현재 사용자 ID
    private String userId;

    // ✅ 사용자의 카테고리 리스트 (optional: you can populate this server-side if preferred)
    private List<String> userCategories;
}