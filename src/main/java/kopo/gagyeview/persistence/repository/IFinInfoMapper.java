package kopo.gagyeview.persistence.repository;

import kopo.gagyeview.dto.MonTrnsDTO;

import java.util.List;

public interface IFinInfoMapper {

    // insert one transaction
    int insertTrns(MonTrnsDTO pDTO);

    // find all transactions by userId and month
    List<MonTrnsDTO> getTrnsByUserAndMon(String userId, String yrMon);

    // delete transaction by id
    int deleteTrnsById(String id);
}
