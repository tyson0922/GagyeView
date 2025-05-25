package kopo.gagyeview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.gagyeview.dto.MartDTO;
import kopo.gagyeview.service.IMapService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Service
public class MapService implements IMapService {

    @Value("${KakaoRestApiKey}")
    private String apiKey;

    private Map<String,String> setKakaoRestApiInfo(){
        Map<String,String> requestHeader = new HashMap<>();
        requestHeader.put("Authorization", "KakaoAK " + apiKey);
        log.info("requestHeader: {}", requestHeader);

        return requestHeader;
    }


    @Override
    public List<MartDTO> searchMart(MartDTO pDTO) throws Exception {
        log.info("{}.searchMart Start!", this.getClass().getName());

        String searchName = CmmUtil.nvl(pDTO.mName());

        // 호출할 Kakao Rest API 정보 설정
        String param = "?query=" + URLEncoder.encode(searchName, "UTF-8");

        // kakao rest API full URL
        String fullUrl = IMapService.keywordSearchUrl + param;
        // KakaoSearch API 호출하기
        String json = NetworkUtil.get(fullUrl, setKakaoRestApiInfo());

        // Json 변수 형태
        log.info("json: {}", json);

        // JSON 구조를 Map 데이터 구조로 변경하기
        // 키와 값 구조의 JSON 구조로부터 데이터를 쉽게 가져오기 위해 Map 데이터구조로 변경ㅎ람
        Map<String, Object> rMap = new ObjectMapper().readValue(json, LinkedHashMap.class);

        // 마트 리스트에서 필요한 정보만 가져오기
        List<Map<String, Object>> documents = (List<Map<String, Object>>) rMap.get("documents");

        List<MartDTO> pList = new LinkedList<>();

        for (int i = 0; i < Math.min(documents.size(), 10); i++) {
            Map<String, Object> doc = documents.get(i);

            String mName = (String) doc.get("place_name");
            String mAddress = (String) doc.get("address_name");
            String mPhoneNm = (String) doc.get("phone");
            String mUrl = (String) doc.get("place_url");
            String x = (String) doc.get("x");
            String y = (String) doc.get("y");

            log.info("mName: {}, mAddress: {}, mPhoneNm: {}, mUrl: {}, x: {}, y: {}", mName, mAddress, mPhoneNm, mUrl, x, y);

            MartDTO rDTO = MartDTO.builder()
                    .mName(mName)
                    .mPhoneNm(mPhoneNm)
                    .mAddress(mAddress)
                    .mUrl(mUrl).x(x).y(y).build();

            pList.add(rDTO);
        }

        log.info("{}.searchMart End!", this.getClass().getName());
        return pList;
    }
}
