package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.*;
import kopo.gagyeview.feign.ChatGptFeignClient;
import kopo.gagyeview.persistence.mapper.ISumMapper;
import kopo.gagyeview.persistence.repository.AbstractMongoDBCommon;
import kopo.gagyeview.persistence.repository.IFinInfoMapper;
import kopo.gagyeview.service.ICatService;
import kopo.gagyeview.service.IFinInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinInfoService extends AbstractMongoDBCommon implements IFinInfoService {

    private final IFinInfoMapper finInfoMapper;
    private final ICatService catService;
    private final ISumMapper sumMapper;
    private final MongoTemplate mongodb;
    private final ChatGptFeignClient chatGptFeignClient;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    // 사용할 컬렉션 이름
    private static final String colNm = "MON_TRNS";

    /**
     * 거래 정보 1건 저장 (컬렉션이 없으면 생성)
     *
     * @param pDTO 저장할 거래 정보 DTO
     * @return 저장 결과 (1: 성공)
     * @throws Exception 예외 발생 시
     */
    @Override
    public int insertTrns(MonTrnsDTO pDTO) throws Exception {
        log.info("{}.insertTrns Start!", this.getClass().getName());

        // 컬렉션이 존재하지 않으면 생성
        if (super.createCollection(mongodb, colNm)) {
            log.info("Collection '{}' created!", colNm);
        }

        int res = finInfoMapper.insertTrns(pDTO);


        // ✅ 사용자 카테고리 동기화
        UserCatDTO catDTO = new UserCatDTO();
        catDTO.setUserId(pDTO.userId());
        catDTO.setCatNm(pDTO.monTrnsDetailDTO().catNm());
        catDTO.setCatType(pDTO.catType());
        catDTO.setRegId(pDTO.userId());
        catDTO.setChgId(pDTO.userId());

        catService.syncUserCat(catDTO);

        pDTO = MonTrnsDTO.builder()
                .id(pDTO.id())
                .userId(pDTO.userId())
                .yrMon(pDTO.getDerivedYrMon()) // ✅ corrected value
                .catType(pDTO.catType())
                .monTrnsDetailDTO(pDTO.monTrnsDetailDTO())
                .regDt(pDTO.regDt())
                .chgDt(pDTO.chgDt())
                .build();

        // ✅ 요약 테이블 동기화
        sumMapper.upsertMonSum(pDTO);
        sumMapper.upsertMonCatSum(pDTO);
        sumMapper.recalculateCatPerc(pDTO);


        log.info("{}.insertTrns End!", this.getClass().getName());
        return res;
    }

    /**
     * 사용자 ID와 연월로 거래 목록 조회
     *
     * @param userId 사용자 ID
     * @param yrMon 연월
     * @return 거래 목록
     * @throws Exception 예외 발생 시
     */
    @Override
    public List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon) {
        log.info("{}.getTrnsByUserAndMon Start!", this.getClass().getName());

        List<MonTrnsDTO> res = finInfoMapper.getTrnsByUserAndMon(userId, yrMon);

        log.info("{}.getTrnsByUserAndMon End!", this.getClass().getName());
        return res;
    }

    /**
     * 거래 ID로 거래 정보 삭제
     *
     * @param id 거래 고유 ID
     * @return 삭제된 개수
     * @throws Exception 예외 발생 시
     */
    @Override
    public int deleteTrnsById(String id) {
        log.info("{}.deleteTrnsById Start!", this.getClass().getName());

        MonTrnsDTO oldDTO = finInfoMapper.getTrnsById(id);

        if (oldDTO != null) {

            oldDTO = MonTrnsDTO.builder()
                    .id(oldDTO.id())
                    .userId(oldDTO.userId())
                    .yrMon(oldDTO.getDerivedYrMon()) // ✅ corrected value
                    .catType(oldDTO.catType())
                    .monTrnsDetailDTO(oldDTO.monTrnsDetailDTO())
                    .regDt(oldDTO.regDt())
                    .chgDt(oldDTO.chgDt())
                    .build();

            sumMapper.decrementMonSum(oldDTO);
            sumMapper.decrementMonCatSum(oldDTO);
            sumMapper.recalculateCatPerc(oldDTO);

            sumMapper.deleteZeroSum(oldDTO);
            sumMapper.deleteZeroCatSum(oldDTO);
        }

        int res = finInfoMapper.deleteTrnsById(id);

        log.info("{}.deleteTrnsById End!", this.getClass().getName());
        return res;
    }


    @Override
    public int updateTrns(MonTrnsDTO pDTO) {
        log.info("{}.updateTrns Start!", this.getClass().getName());

        // Step 1: Get old transaction by ID
        MonTrnsDTO oldDTO = finInfoMapper.getTrnsById(pDTO.id());

        // Before decrement & upsert
        oldDTO = MonTrnsDTO.builder()
                .id(oldDTO.id())
                .userId(oldDTO.userId())
                .yrMon(oldDTO.getDerivedYrMon()) // ✅ corrected value
                .catType(oldDTO.catType())
                .monTrnsDetailDTO(oldDTO.monTrnsDetailDTO())
                .regDt(oldDTO.regDt())
                .chgDt(oldDTO.chgDt())
                .build();

        pDTO = MonTrnsDTO.builder()
                .id(pDTO.id())
                .userId(pDTO.userId())
                .yrMon(pDTO.getDerivedYrMon()) // ✅ corrected value
                .catType(pDTO.catType())
                .monTrnsDetailDTO(pDTO.monTrnsDetailDTO())
                .regDt(pDTO.regDt())
                .chgDt(pDTO.chgDt())
                .build();

        if (oldDTO != null) {
            // Step 2: Decrement the old values from summary tables
            sumMapper.decrementMonSum(oldDTO);
            sumMapper.decrementMonCatSum(oldDTO);
            sumMapper.recalculateCatPerc(oldDTO);

            sumMapper.deleteZeroSum(oldDTO);
            sumMapper.deleteZeroCatSum(oldDTO);
        }

        // Step 3: Update transaction in MongoDB
        int res = finInfoMapper.updateTrns(pDTO);

        // Step 4: Add new values to summary tables
        sumMapper.upsertMonSum(pDTO);
        sumMapper.upsertMonCatSum(pDTO);
        sumMapper.recalculateCatPerc(pDTO);



        log.info("{}.updateTrns End!", this.getClass().getName());
        return res;
    }

    @Override
    public List<MonTrnsDTO> getTrnsByDateRange(String userId, String startDate, String endDate) throws Exception {
        log.info("{}.getTrnsByDateRange Start!", this.getClass().getName());

        List<MonTrnsDTO> res = finInfoMapper.getTrnsByDateRange(userId, startDate, endDate);

        log.info("{}.getTrnsByDateRange End!", this.getClass().getName());
        return res;
    }

    @Override
    public List<DonutChartDTO> getDonutByCatType(String userId, String catType, String yrMon) {
        log.info("🎯 [SERVICE] getDonutByCatType() 시작 - userId: {}, catType: {}, yrMon: {}", userId, catType, yrMon);

        List<DonutChartDTO> rList = sumMapper.getDonutByCatType(userId, catType, yrMon);

        if (rList == null || rList.isEmpty()) {
            log.warn("⚠️ [SERVICE] getDonutByCatType 결과 없음 (userId: {}, catType: {}, yrMon: {})", userId, catType, yrMon);
        } else {
            log.info("✅ [SERVICE] getDonutByCatType 결과 건수: {}", rList.size());
            for (DonutChartDTO dto : rList) {
                log.debug("📌 도넛 항목 - name: {}, value: {}", dto.getName(), dto.getValue());
            }
        }

        return rList != null ? rList : List.of();
    }

    @Override
    public List<BarChartDTO> getMonthlyIncomeExpense(String userId) {
        log.info("🎯 [SERVICE] getMonthlyIncomeExpense() 시작 - userId: {}", userId);

        List<BarChartDTO> rList = sumMapper.getMonthlyIncomeExpense(userId);
        log.info("✅ [SERVICE] getMonthlyIncomeExpense 결과 건수: {}", rList.size());

        return rList;
    }

    @Override
    public List<StackBarDTO> getMonthlyStack(String userId, String catType) {
        log.info("getMonthlyStack - userId: {}, catType: {}", userId, catType);
        return sumMapper.getMonthlyStack(userId, catType);
    }

    @Override
    public BigDecimal getTotalAmountByType(String userId, String catType) {
        log.info("getTotalAmountByType - userId: {}, catType: {}", userId, catType);
        return sumMapper.getTotalAmountByType(userId, catType);
    }

    @Override
    public BigDecimal getMonthlyTotal(String userId, String catType, String yrMon) {
        log.info("getMonthlyTotal - userId: {}, catType: {}, yrMon: {}", userId, catType, yrMon);
        return sumMapper.getMonthlyTotal(userId, catType, yrMon);
    }

    /**
     * AI를 통한 사용자 소비 습관 요약
     * @param userId 사용자 ID
     * @return AI가 요약한 소비 습관
     */
    public String getAiSpendingSummary(String userId) {
        // 1. 사용자 월별 요약 데이터 준비
        List<BarChartDTO> summaryList = sumMapper.getMonthlyIncomeExpense(userId);
        if (summaryList == null || summaryList.isEmpty()) {
            log.warn("sumMapper.getMonthlyIncomeExpense returned no data for userId={}", userId);
        } else {
            log.info("sumMapper.getMonthlyIncomeExpense returned {} items for userId={}", summaryList.size(), userId);
            for (BarChartDTO dto : summaryList) {
                log.info("MonthlyIncomeExpense: month={}, income={}, expense={}", dto.getMonth(), dto.getIncome(), dto.getExpense());
            }
        }
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("아래는 사용자의 월별 수입/지출 요약입니다. 소비 습관을 분석해 한글로 요약해 주세요.\n");
        for (BarChartDTO dto : summaryList) {
            promptBuilder.append(String.format("%s월: 수입=%.0f, 지출=%.0f\n", dto.getMonth(), dto.getIncome(), dto.getExpense()));
        }
        String userPrompt = promptBuilder.toString();

        // 2. 더 명확한 시스템 프롬프트와 예시 추가
        String systemPrompt = "You are a financial assistant. Summarize the user's monthly income and spending habits in Korean.\n" +
                "Example:\nInput:\n2025-04월: 수입=75349, 지출=24309\n2025-05월: 수입=81136, 지출=32463\n2025-06월: 수입=78946, 지출=33324\n\nOutput:\n사용자는 4월~6월 동안 꾸준히 수입이 발생했으며, 지출은 수입에 비해 적은 편입니다. 5월에 지출이 다소 증가했으나 전체적으로 건전한 소비 습관을 보입니다.";

        // 3. 메시지 배열 구성
        ChatGptRequestDTO.Message systemMessage = ChatGptRequestDTO.Message.builder()
            .role("system")
            .content(systemPrompt)
            .build();
        ChatGptRequestDTO.Message userMessage = ChatGptRequestDTO.Message.builder()
            .role("user")
            .content(userPrompt)
            .build();
        List<ChatGptRequestDTO.Message> messages = List.of(systemMessage, userMessage);
        log.info("AI system prompt: {}", systemPrompt);
        log.info("AI user prompt: {}", userPrompt);
        log.info("AI messages: {}", messages);

        // 4. 모델명 최신화 (gpt-4o 권장)
        ChatGptRequestDTO requestDTO = ChatGptRequestDTO.builder()
            .model("gpt-4o")
            .messages(messages)
            .max_completion_tokens(300)
            .build();

        // 5. OpenAI API 키
        String apiKey = openaiApiKey;
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("openai.api-key property가 필요합니다.");
        String authorization = "Bearer " + apiKey;

        // 6. Feign 클라이언트 호출
        ChatGptResponseDTO response = chatGptFeignClient.getSummary(authorization, requestDTO);
        log.info("AI raw response: {}", abbreviate(String.valueOf(response), 1000));
        String summary = null;
        summary = extractContentSafely(response);
        log.info("AI summary raw: {}", abbreviate(summary, 800));
        // 마크다운 펜스 제거
        summary = stripFences(summary);
        if (summary == null || summary.isBlank()) {
            log.warn("AI 요약 결과를 가져올 수 없습니다. (response or message is null/blank)");
            return "AI 요약 결과를 가져올 수 없습니다.";
        }
        log.info("AI summary cleaned: {}", abbreviate(summary, 800));
        return summary;
    }

    /**
     * Safely extract content from OpenAI response (robust to future changes)
     */
    private static String extractContentSafely(ChatGptResponseDTO response) {
        if (response == null) return null;
        // Standard OpenAI Chat Completions
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            ChatGptResponseDTO.Choice choice = response.getChoices().get(0);
            if (choice != null && choice.getMessage() != null && choice.getMessage().getContent() != null) {
                String s = choice.getMessage().getContent();
                if (!s.isBlank()) return s;
            }
            // If other fields are added in future, handle here
        }
        // Ultra fallback
        return null;
    }

    /**
     * Remove markdown code fences from AI output
     */
    private static String stripFences(String content) {
        if (content == null) return null;
        String s = content.trim();
        s = s.replaceAll("(?s)^```json\\s*", "");
        s = s.replaceAll("(?s)^```\\s*", "");
        s = s.replaceAll("\\s*```\\s*$", "");
        return s.trim();
    }

    /**
     * Abbreviate long strings for logging
     */
    private static String abbreviate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max)) + " …(" + s.length() + " chars)";
    }

}
