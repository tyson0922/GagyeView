package kopo.gagyeview.controller;

import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.validation.OnCheckUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping(value="/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    private final IUserInfoService userInfoService;

    /**
     * 회원가입 화면으로 이동
     */
    @GetMapping(value="/userRegForm")
    public String userRegForm() {
        log.info("{}.userRegForm", this.getClass().getName());

        return "/user/userRegForm";
    }

    /**
     * 회원 가입 전 아이디 중복체크하기( AJAX를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value="/getUserIdExists")
    public ResponseEntity<? extends CommonResponse<?>> getUserIdExists(
            @Validated(OnCheckUserId.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        // DTO 유효성 검사 실패 시 에러 응답 반환
        if(bindingResult.hasErrors()) { // Spring Validation 맞춰 잘 바인딩 되었는지 체크
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 회원 아이디
        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("{}.userId received: {}",this.getClass().getName(),userId);

        // 회원아이디를 통해 중복된 아이디인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO)).orElse(UserInfoDTO.builder().build());

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "success", rDTO)
        );
    }

}
