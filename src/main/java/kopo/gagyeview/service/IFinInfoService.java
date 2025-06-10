package kopo.gagyeview.service;

import kopo.gagyeview.dto.AggregationResultDTO;
import kopo.gagyeview.dto.MonTrnsDTO;

import java.util.List;

public interface IFinInfoService {

    /**
     * 거래 정보 1건 저장
     *
     * @param pDTO 저장할 거래 정보 DTO
     * @return 성공 시 1
     * @throws Exception 예외 발생 시
     */
    int insertTrns(MonTrnsDTO pDTO) throws Exception;

    /**
     * 사용자 ID와 연월로 거래 목록 조회
     *
     * @param userId 사용자 ID
     * @param yrMon 연월(예: 2025-05)
     * @return 거래 목록
     * @throws Exception 예외 발생 시
     */
    List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon) throws Exception;

    /**
     * 거래 ID로 거래 정보 삭제
     *
     * @param id 거래 고유 ID
     * @return 삭제된 개수
     * @throws Exception 예외 발생 시
     */
    int deleteTrnsById(String id) throws Exception;

    /**
     * 거래 정보 수정
     * @param pDTO 수정할 거래 정보
     * @return 성공 시 1
     * @throws Exception 예외 발생 시
     */
    int updateTrns(MonTrnsDTO pDTO) throws Exception;

    List<MonTrnsDTO> getTrnsByDateRange(String userId, String startDate, String endDate) throws Exception;

    List<AggregationResultDTO> monTotalByType(String userId) throws Exception;

    List<AggregationResultDTO> monIncomeExpense(String userId) throws Exception;

    List<AggregationResultDTO> monthlyCategoryStack(String userId) throws Exception;
}
