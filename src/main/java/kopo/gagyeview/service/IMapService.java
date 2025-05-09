package kopo.gagyeview.service;

import kopo.gagyeview.dto.MartDTO;

import java.util.List;

public interface IMapService {

    String keywordSearchUrl =  "https://dapi.kakao.com/v2/local/search/keyword.json";

    List<MartDTO> searchMart(MartDTO pDTO) throws Exception;

}
