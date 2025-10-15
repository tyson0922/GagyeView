package kopo.gagyeview.controller;

import jakarta.validation.Valid;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.MartDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.service.IMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(value="/map")
public class MapController {

    @Value("${KakaoJsApiKey}")
    private String kakaoJsKey;

    private final IMapService mapService;


    @GetMapping(value="")
    public String mapPage(Model model){
        log.info("{}.mapPage Start!", this.getClass().getName());

        model.addAttribute("kakaoJsKey", kakaoJsKey);

        log.info("{}.mapPage End!", this.getClass().getName());
        return "map/map";
    }

    @PostMapping("/searchMart")
    @ResponseBody
    public ResponseEntity<? extends CommonResponse<?>> searchMart(
            @Valid @RequestBody MartDTO pDTO, BindingResult bindingResult) throws Exception {

        log.info("{}.searchMart Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String samTitle = "";
        String samText = "";
        try {
            List<MartDTO> rList = mapService.searchMart(pDTO);

            if (rList == null || rList.isEmpty()) {
                samTitle = "검색 결과 없음";
                samText = "해당 이름의 마트를 찾을 수 없습니다.";
                return ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", SweetAlertMsgDTO.fail(samTitle, samText)
                ));
            }

            log.info("{}.searchMart End!", this.getClass().getName());
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", rList));

        } catch (Exception e) {
            log.error("{}.searchMart Error!", this.getClass().getName(), e);

            samTitle = "시스템 오류";
            samText = "마트 검색 중 오류가 발생했습니다.";
            return ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR, "fail", SweetAlertMsgDTO.fail(samTitle, samText)
            ));
        }
    }
}
