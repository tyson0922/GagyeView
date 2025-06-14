//package kopo.gagyeview.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kopo.gagyeview.dto.MonTrnsDTO;
//import kopo.gagyeview.dto.MonTrnsDetailDTO;
//import kopo.gagyeview.service.IFinInfoService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MonTrnsDataLoader implements CommandLineRunner {
//
//    private final MongoTemplate mongoTemplate;
//    private final IFinInfoService finInfoService;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void run(String... args) {
//        log.info("📌 Loading sample transaction data...");
//
//        String userId = "tyson1234";
//        List<MonTrnsDTO> sampleData = new ArrayList<>();
//
//        Map<String, String> expCategories = Map.ofEntries(
//                Map.entry("식비", "스타벅스"),
//                Map.entry("교통비", "지하철"),
//                Map.entry("주거비", "월세"),
//                Map.entry("문화생활", "넷플릭스"),
//                Map.entry("기타지출", "문구류")
//        );
//
//        Map<String, String> incCategories = Map.ofEntries(
//                Map.entry("월급", "편의점 아르바이트"),
//                Map.entry("용돈", "부모님"),
//                Map.entry("이자소득", "토스이자"),
//                Map.entry("투자수익", "주식배당"),
//                Map.entry("기타수입", "중고나라")
//        );
//
//        Random rand = new Random();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//        for (int month = 4; month <= 6; month++) {
//            for (int i = 1; i <= 5; i++) { // 5 expense
//                String catNm = expCategories.keySet().stream().toList().get(rand.nextInt(expCategories.size()));
//                String srcNm = expCategories.get(catNm);
//                Date trnsDt = getRandomDate(2025, month);
//                String yrMon = sdf.format(trnsDt).substring(0, 7); // yyyy-MM
//
//                MonTrnsDTO dto = MonTrnsDTO.builder()
//                        .userId(userId)
//                        .yrMon(yrMon)
//                        .catType("지출")
//                        .monTrnsDetailDTO(
//                                MonTrnsDetailDTO.builder()
//                                        .catNm(catNm)
//                                        .trnsDt(trnsDt)
//                                        .totNm(BigDecimal.valueOf(rand.nextInt(9000) + 1000))
//                                        .srcNm(srcNm)
//                                        .note("메모 없음")
//                                        .build()
//                        )
//                        .regDt(new Date())
//                        .chgDt(new Date())
//                        .build();
//
//                sampleData.add(dto);
//            }
//
//            for (int i = 1; i <= 5; i++) { // 5 income
//                String catNm = incCategories.keySet().stream().toList().get(rand.nextInt(incCategories.size()));
//                String srcNm = incCategories.get(catNm);
//                Date trnsDt = getRandomDate(2025, month);
//                String yrMon = sdf.format(trnsDt).substring(0, 7); // yyyy-MM
//
//                MonTrnsDTO dto = MonTrnsDTO.builder()
//                        .userId(userId)
//                        .yrMon(yrMon)
//                        .catType("수입")
//                        .monTrnsDetailDTO(
//                                MonTrnsDetailDTO.builder()
//                                        .catNm(catNm)
//                                        .trnsDt(trnsDt)
//                                        .totNm(BigDecimal.valueOf(rand.nextInt(20000) + 5000))
//                                        .srcNm(srcNm)
//                                        .note("정기 수입")
//                                        .build()
//                        )
//                        .regDt(new Date())
//                        .chgDt(new Date())
//                        .build();
//
//                sampleData.add(dto);
//            }
//        }
//
//        for (MonTrnsDTO dto : sampleData) {
//            try {
//                finInfoService.insertTrns(dto);
//            } catch (Exception e) {
//                log.error("❌ Failed to insert transaction: {}", dto, e);
//            }
//        }
//
//
//        log.info("✅ Sample data inserted: {} entries", sampleData.size());
//    }
//
//    private Date getRandomDate(int year, int month) {
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, year);
//        cal.set(Calendar.MONTH, month - 1); // Java months start at 0
//        cal.set(Calendar.DAY_OF_MONTH, new Random().nextInt(27) + 1);
//        return cal.getTime();
//    }
//}
