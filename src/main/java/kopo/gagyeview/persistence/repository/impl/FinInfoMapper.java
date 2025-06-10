package kopo.gagyeview.persistence.repository.impl;

import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import kopo.gagyeview.dto.AggregationResultDTO;
import kopo.gagyeview.dto.MonTrnsDTO;
import kopo.gagyeview.persistence.repository.IFinInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FinInfoMapper implements IFinInfoMapper {

    // MongoDB와 연결하기 위한 Spring Data MongoTemplate
    private final MongoTemplate mongodb;

    // 사용할 컬렉션 이름
    private static final String colNm = "MON_TRNS";

    /**
     * 거래 정보 1건을 MongoDB에 저장
     *
     * @param pDTO 저장할 거래 정보 DTO
     * @return 성공 시 1 반환
     * @throws MongoException MongoDB 오류 발생 시
     */
    @Override
    public int insertTrns(MonTrnsDTO pDTO) throws MongoException {
        // DTO 데이터를 MongoDB에 insert
        mongodb.insert(pDTO, colNm);

        int res = 1; // 성공적으로 insert되면 1 반환
        return res;
    }

    /**
     * 사용자 ID와 연월(예: 2025-05)에 해당하는 거래 목록 조회
     *
     * @param userId 사용자 ID
     * @param yrMon 연월(yyyy-MM 형식)
     * @return 조회된 거래 목록
     */
    @Override
    public List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon) throws MongoException{
        // 검색 조건 설정 (userId AND yrMon)
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("yrMon").is(yrMon));


        // ✅ 최신순 정렬 추가 (거래일시 기준)
        query.with(Sort.by(Sort.Direction.DESC, "monTrnsDetailDTO.trnsDt"));

        // 조건에 맞는 거래 목록 조회
        List<MonTrnsDTO> res = mongodb.find(query, MonTrnsDTO.class, colNm);
        return res;
    }

    /**
     * 거래 고유 ID(_id)를 기준으로 해당 거래 데이터 삭제
     *
     * @param id 삭제할 거래의 MongoDB _id
     * @return 삭제된 데이터 개수
     */
    @Override
    public int deleteTrnsById(String id) throws MongoException {
        // _id 값을 기준으로 삭제 조건 생성
        Query query = new Query(Criteria.where("_id").is(id));

        // 삭제 실행 및 결과 수 반환
        int res = (int) mongodb.remove(query, MonTrnsDTO.class, colNm).getDeletedCount();
        return res;
    }

    @Override
    public int updateTrns(MonTrnsDTO pDTO) {
        Query query = new Query(Criteria.where("_id").is(pDTO.id()));

        Update update = new Update()
                .set("monTrnsDetailDTO", pDTO.monTrnsDetailDTO())
                .set("catType", pDTO.catType())
                .set("chgDt", new Date()); // Optional: Update modified timestamp

        UpdateResult result = mongodb.updateFirst(query, update, MonTrnsDTO.class, colNm);
        return (int) result.getModifiedCount();
    }

    @Override
    public List<MonTrnsDTO> getTrnsByDateRange(String userId, String startDate, String endDate) throws Exception {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));

        // ✅ Convert ISO yyyy-MM-dd to java.util.Date
        if (startDate != null && !startDate.isEmpty() &&
                endDate != null && !endDate.isEmpty()) {

            // Start of day
            Date start = java.sql.Date.valueOf(startDate);

            // End of day (23:59:59.999)
            LocalDate endLocal = LocalDate.parse(endDate);
            Date end = Date.from(endLocal.atTime(23, 59, 59, 999_000_000)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());

            query.addCriteria(Criteria.where("monTrnsDetailDTO.trnsDt").gte(start).lte(end));
        }

        query.with(Sort.by(Sort.Direction.DESC, "monTrnsDetailDTO.trnsDt"));
        return mongodb.find(query, MonTrnsDTO.class, colNm);
    }

    @Override
    public List<AggregationResultDTO> monTotalByType(String userId) throws Exception {
        MatchOperation match = match(Criteria.where("userId").is(userId));

        // Convert totNm (String) to number
        AddFieldsOperation addFields = AddFieldsOperation.addField("amount")
                .withValue(ConvertOperators.ToDouble.toDouble("$monTrnsDetailDTO.totNm"))
                .build();

        GroupOperation group = group("yrMon", "catType")
                .sum("amount").as("total");

        ProjectionOperation project = project()
                .and("_id.yrMon").as("yrMon")
                .and("_id.catType").as("catType")
                .and("total").as("total");

        Aggregation agg = newAggregation(match, addFields, group, project);
        return mongodb.aggregate(agg, colNm, AggregationResultDTO.class).getMappedResults();
    }

    @Override
    public List<AggregationResultDTO> monIncomeExpense(String userId) throws Exception {
        MatchOperation match = match(Criteria.where("userId").is(userId));

        GroupOperation group = group("yrMon", "catType")
                .sum("monTrnsDetailDTO.totNm").as("amount");

        GroupOperation pivot = group("_id.yrMon")
                .sum(ConditionalOperators.when(ComparisonOperators.valueOf("_id.catType").equalToValue("수입")).then("amount").otherwise(0)).as("income")
                .sum(ConditionalOperators.when(ComparisonOperators.valueOf("_id.catType").equalToValue("지출")).then("amount").otherwise(0)).as("expense");

        ProjectionOperation project = project()
                .and("_id").as("yrMon")
                .and("income").as("income")
                .and("expense").as("expense");

        Aggregation agg = newAggregation(match, group, pivot, project);

        return mongodb.aggregate(agg, colNm, AggregationResultDTO.class).getMappedResults();
    }

    @Override
    public List<AggregationResultDTO> monthlyCategoryStack(String userId) throws Exception {
        MatchOperation match = match(Criteria.where("userId").is(userId));

        AddFieldsOperation addFields = AddFieldsOperation.addField("amount")
                .withValue(ConvertOperators.ToDouble.toDouble("$monTrnsDetailDTO.totNm"))
                .build();

        GroupOperation group = group("yrMon", "catType", "monTrnsDetailDTO.catNm")
                .sum("amount").as("total");

        ProjectionOperation project = project()
                .and("_id.yrMon").as("yrMon")
                .and("_id.catType").as("catType")
                .and("_id.catNm").as("catNm")
                .and("total").as("total");

        Aggregation agg = newAggregation(match, addFields, group, project);
        return mongodb.aggregate(agg, colNm, AggregationResultDTO.class).getMappedResults();
    }


}
