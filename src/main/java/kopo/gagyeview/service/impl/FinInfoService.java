package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.*;
import kopo.gagyeview.persistence.mapper.ISumMapper;
import kopo.gagyeview.persistence.repository.AbstractMongoDBCommon;
import kopo.gagyeview.persistence.repository.IFinInfoMapper;
import kopo.gagyeview.service.ICatService;
import kopo.gagyeview.service.IFinInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon) throws Exception {
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
    public int deleteTrnsById(String id) throws Exception {
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
    public int updateTrns(MonTrnsDTO pDTO) throws Exception {
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
    public List<DonutChartDTO> getDonutByCatType(String userId, String catType, String yrMon) throws Exception {
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
    public List<BarChartDTO> getMonthlyIncomeExpense(String userId) throws Exception {
        log.info("🎯 [SERVICE] getMonthlyIncomeExpense() 시작 - userId: {}", userId);

        List<BarChartDTO> rList = sumMapper.getMonthlyIncomeExpense(userId);
        log.info("✅ [SERVICE] getMonthlyIncomeExpense 결과 건수: {}", rList.size());

        return rList;
    }

    @Override
    public List<StackBarDTO> getMonthlyStack(String userId, String catType) throws Exception {
        log.info("getMonthlyStack - userId: {}, catType: {}", userId, catType);
        return sumMapper.getMonthlyStack(userId, catType);
    }

    @Override
    public BigDecimal getTotalAmountByType(String userId, String catType) throws Exception {
        log.info("getTotalAmountByType - userId: {}, catType: {}", userId, catType);
        return sumMapper.getTotalAmountByType(userId, catType);
    }

    @Override
    public BigDecimal getMonthlyTotal(String userId, String catType, String yrMon) throws Exception {
        log.info("getMonthlyTotal - userId: {}, catType: {}, yrMon: {}", userId, catType, yrMon);
        return sumMapper.getMonthlyTotal(userId, catType, yrMon);
    }

}
