package kopo.gagyeview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import kopo.gagyeview.dto.MonTrnsDTO;
import kopo.gagyeview.dto.MonTrnsDetailDTO;
import kopo.gagyeview.persistence.mapper.ICatMapper;
import kopo.gagyeview.service.IFinInfoService;
import kopo.gagyeview.service.IScanService;
import kopo.gagyeview.util.NetworkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService implements IScanService {

    private final ObjectMapper objectMapper;
    private final IFinInfoService finInfoService;
    private final ICatMapper CatMapper;

    //    @Value("file:${google.cloud.credential.path}")
    @Value("file:${GOOGLE_APPLICATION_CREDENTIALS}")
    private Resource googleCredentialsResource;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * ✅ Sets GOOGLE_APPLICATION_CREDENTIALS so Google SDK can work without manually passing credentials.
     */
    @PostConstruct
    public void initGoogleCredentials() throws Exception {
        File jsonFile = googleCredentialsResource.getFile();
        String absolutePath = jsonFile.getAbsolutePath();
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", absolutePath);
        log.info("✅ GOOGLE_APPLICATION_CREDENTIALS set to: {}", absolutePath);
    }

    /**
     * ✅ Google Cloud Vision OCR
     */
    @Override
    public String callGoogleVisionAPI(String base64Image) {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(googleCredentialsResource.getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create(settings)) {

                ByteString imgBytes = ByteString.copyFrom(Base64.getDecoder().decode(base64Image));

                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder()
                                .addFeatures(feat)
                                .setImage(img)
                                .build();

                List<AnnotateImageRequest> requests = List.of(request);
                BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(requests);
                AnnotateImageResponse res = response.getResponses(0);

                if (res.hasError()) {
                    log.error("🛑 Vision API 오류: {}", res.getError().getMessage());
                    return "";
                }

                return res.getTextAnnotationsList().isEmpty()
                        ? "No text found."
                        : res.getTextAnnotationsList().get(0).getDescription();
            }

        } catch (Exception e) {
            log.error("🛑 Vision API 호출 실패", e);
            return "";
        }
    }

    /**
     * ✅ ChatGPT 분류 + 응답 JSON 파싱
     */
    @Override
    public JsonNode callChatGPTAPI(String ocrText, List<String> userCategories) {
        String url = "https://api.openai.com/v1/chat/completions";

        // ✅ 프롬프트 생성
        StringBuilder sb = new StringBuilder();
        sb.append("Here is the scanned receipt text:\n\n")
                .append(ocrText).append("\n\n")
                .append("The user's available categories are:\n");

        for (String cat : userCategories) {
            sb.append("- ").append(cat).append("\n");
        }

        sb.append("\nReturn the result as JSON with the following fields:\n")
                .append("- catType (수입 or 지출)\n")
                .append("- catNm (must be from the list above)\n")
                .append("- trnsDt (must be in yyyy-MM-dd format, do NOT use words like 'today')\n")
                .append("- trnsAmt (numeric, total amount spent)\n")
                .append("- memo (brief description)\n")
                .append("\nReply with only the JSON object, no explanation.");

        Map<String, Object> requestMap = Map.of(
                "model", "gpt-4",
                "messages", List.of(Map.of("role", "user", "content", sb.toString()))
        );

        try {
            String postBody = objectMapper.writeValueAsString(requestMap);

            Map<String, String> headers = Map.of(
                    "Authorization", "Bearer " + openaiApiKey,
                    "Content-Type", "application/json"
            );

            String responseJson = NetworkUtil.post(url, headers, postBody);

            String content = objectMapper
                    .readTree(responseJson)
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return objectMapper.readTree(content);

        } catch (Exception e) {
            log.error("🔴 ChatGPT API 호출 또는 응답 파싱 실패", e);
            return null;
        }
    }

    /**
     * ✅ GPT JSON → MonTrnsDTO 변환
     */
    public MonTrnsDTO convertGptJsonToDTO(JsonNode json, String userId) throws Exception {

        String catType = json.path("catType").asText();
        String catNm = json.path("catNm").asText();
        String trnsDtStr = json.path("trnsDt").asText();
        BigDecimal totNm = new BigDecimal(json.path("trnsAmt").asText("0"));
        String note = json.path("memo").asText();

        Date trnsDt = trnsDtStr.isBlank() ? new Date() : dateFormat.parse(trnsDtStr);
        // ✅ Extract "YYYY-MM" from trnsDt
        String yrMon = new SimpleDateFormat("yyyy-MM").format(trnsDt);

        return MonTrnsDTO.builder()
                .userId(userId)
                .catType(catType)
                .yrMon(yrMon) // ✅ set it here!
                .monTrnsDetailDTO(MonTrnsDetailDTO.builder()
                        .catNm(catNm)
                        .trnsDt(trnsDt)
                        .totNm(totNm)
                        .note(note)
                        .build())
                .build();
    }

    /**
     * ✅ OCR 결과 기반 → ChatGPT 분석 + 저장
     */
    @Override
    public Map<String, Object> analyzeAndSaveTransaction(String ocrText, String userId) throws Exception {

        List<String> userCats = CatMapper.selectCatNamesByUserId(userId);
        if (userCats.isEmpty()) {
            log.warn("📋 사용자 카테고리 없음");
            return null;
        }

        JsonNode gptJson = callChatGPTAPI(ocrText, userCats);
        if (gptJson == null || gptJson.isEmpty()) {
            log.warn("🤖 GPT 응답 없음");
            return null;
        }

        MonTrnsDTO pDTO = convertGptJsonToDTO(gptJson, userId);
        int saveResult = finInfoService.insertTrns(pDTO);

        if (saveResult == 0) {
            log.warn("💾 저장 실패");
            return null;
        }

        // 🎯 분석 결과와 저장 성공 여부를 함께 반환
        return Map.of(
                "parsedResult", gptJson, // GPT 분석 결과
                "saved", true            // 저장 성공 여부
        );
    }
}
