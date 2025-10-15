package kopo.gagyeview.persistence.repository;

import com.mongodb.MongoException;
import kopo.gagyeview.dto.AggregationResultDTO;
import kopo.gagyeview.dto.MonTrnsDTO;

import java.util.List;

public interface IFinInfoMapper {

    /**
     * 거래 정보 1건 저장
     *
     * @param pDTO 저장할 거래 정보
     * @return 저장 성공 시 1
     */
    int insertTrns(MonTrnsDTO pDTO);

    /**
     * 사용자 ID와 연월로 거래 목록 조회
     *
     * @param userId 사용자 ID
     * @param yrMon 연월(예: 2025-05)
     * @return 거래 목록
     */
    List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon);

    /**
     * _id로 거래 삭제
     *
     * @param id 거래 고유 ID
     * @return 삭제된 개수
     */
    int deleteTrnsById(String id);

    /**
     * 사용자 거래 정보 수정
     *
     * @param pDTO 수정할 거래 정보
     * @return 저장 성공 시 1
     */
    int updateTrns(MonTrnsDTO pDTO);

    MonTrnsDTO getTrnsById(String id);

    List<MonTrnsDTO> getTrnsByDateRange(String userId, String startDate, String endDate) throws Exception;

    List<AggregationResultDTO> monTotalByType(String userId) throws Exception;

    List<AggregationResultDTO> monIncomeExpense(String userId) throws Exception;

    List<AggregationResultDTO> monthlyCategoryStack(String userId) throws Exception;

    int deleteTrnsByUserId(String userId) throws MongoException;

}
