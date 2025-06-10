package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserBnkDTO;
import kopo.gagyeview.service.IUserBnkService;
import kopo.gagyeview.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping(value = "/bank")
@RequiredArgsConstructor
@Controller
public class UserBnkController {

    private final IUserBnkService userBnkService;

    /**
     * 계좌 생성 요청 처리 (AJAX)
     */
    @ResponseBody
    @PostMapping(value = "/insertUserBnk")
    public ResponseEntity<? extends CommonResponse<?>> insertUserBnk(
            @Valid @RequestBody UserBnkDTO pDTO, HttpSession session, BindingResult bindingResult) throws Exception {

        log.info("{}.insertUserBnk Start!", this.getClass().getName());

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        if (userId.isEmpty()) {
            samTitle = "세션 만료";
            samText = "로그인을 다시 진행해주세요.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        pDTO.setUserId(userId);
        pDTO.setRegId(userId);
        pDTO.setChgId(userId);
        pDTO.setRegDt(LocalDateTime.now());
        pDTO.setChgDt(LocalDateTime.now());

        log.info("insert DTO: {}", pDTO);
        try {
            int result = userBnkService.insertUserBank(pDTO);
            log.info("insertUserBank result: {}", result);

            if (result == 1) {
                samTitle = "계좌 생성 성공";
                samText = "계좌가 성공적으로 등록되었습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
            } else {
                samTitle = "중복 계좌";
                samText = "이미 동일한 이름의 계좌가 존재합니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.CONFLICT, "fail", samDTO));
            }
        } catch (Exception e) {
            log.error("{}.insertUserBnk Error", this.getClass().getName(), e);
            samTitle = "시스템 오류";
            samText = "계좌 등록 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        log.info("{}.insertUserBnk End!", this.getClass().getName());
        return response;
    }

    /**
     * 계좌 등록 페이지 이동
     */
    @GetMapping(value = "/form")
    public String bankForm(HttpSession session, Model model) {
        log.info("{}.bankForm Start!", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        if (userId.isEmpty()) {
            return "redirect:/user/login";
        }

        try {
            List<UserBnkDTO> userBanks = userBnkService.getUserBankList(userId);
            model.addAttribute("userBanks", userBanks);
        } catch (Exception e) {
            log.error("계좌 조회 중 오류 발생", e);
            model.addAttribute("userBanks", List.of()); // 비어 있는 리스트 전달
        }

        log.info("{}.bankForm End!", this.getClass().getName());
        return "finInfo/bnkInfo";
    }




}
