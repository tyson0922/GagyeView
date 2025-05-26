package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.MonTrnsDTO;
import kopo.gagyeview.persistence.repository.AbstractMongoDBCommon;
import kopo.gagyeview.persistence.repository.IFinInfoMapper;
import kopo.gagyeview.service.IFinInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinInfoService extends AbstractMongoDBCommon implements IFinInfoService {

    private final IFinInfoMapper finInfoMapper;
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

        int res = finInfoMapper.deleteTrnsById(id);

        log.info("{}.deleteTrnsById End!", this.getClass().getName());
        return res;
    }

    @Override
    public int updateTrns(MonTrnsDTO pDTO) throws Exception {
        log.info("{}.updateTrns Start!", this.getClass().getName());

        int res = finInfoMapper.updateTrns(pDTO);

        log.info("{}.updateTrns End!", this.getClass().getName());
        return res;
    }
}
