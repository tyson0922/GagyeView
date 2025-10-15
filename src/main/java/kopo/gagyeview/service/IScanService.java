package kopo.gagyeview.service;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface IScanService {

    /**
     * Google Vision OCR 호출
     * @param base64Image base64 인코딩된 이미지
     * @return 추출된 텍스트
     */
    String callGoogleVisionAPI(String base64Image);

    /**
     * ✅ ChatGPT API 호출 (분류 및 분석 결과 반환)
     * @param ocrText OCR에서 추출된 텍스트
     * @param userCategories 현재 사용자의 카테고리 목록
     * @return GPT 분석 결과 (JSON)
     */
    JsonNode callChatGPTAPI(String ocrText, List<String> userCategories);

    /**
     * ✅ OCR 결과를 기반으로 GPT 분석 → MonTrnsDTO 생성 및 저장
     * @param ocrText OCR 텍스트
     * @param userId 사용자 ID
     * @return 분석 결과와 저장 성공 여부가 포함된 Map (예: {"parsedResult": JsonNode, "saved": true})
     */
    Map<String, Object> analyzeAndSaveTransaction(String ocrText, String userId) throws Exception;

}
