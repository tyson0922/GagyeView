package kopo.gagyeview.controller;

import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.EncryptUtil;
import kopo.gagyeview.validation.OnCheckUserId;
import kopo.gagyeview.validation.OnSendEmail;
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
        ExistsYnDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElse(ExistsYnDTO.builder().build());
        log.info("rDTO: {}", rDTO);
        log.info("rDTO.existsYn : {}", rDTO.existsYn());

        // 아이디는 소분자 대문자 구분 안하기
        if ("Y".equalsIgnoreCase(rDTO.existsYn())){
            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "fail",
                            SweetAlertMsgDTO.fail("중복된 아이디", "이미 존재하는 아이디입니다."))
            );
        }

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "success",
                        SweetAlertMsgDTO.success("사용가능", "사용 가능한 아이디입니다."))
        );
    }

    /**
     * 회원 가입 전 이메일 중복체크하기 (Ajax를 통해 입력한 아이디 정보 받음)
     * 유효한 이메일인지 확인하기 위해 입력된 이메일에 인증번호 포함하여 메일 발송
     */
    @ResponseBody
    @PostMapping(value="getEmailExists")
    public ResponseEntity<? extends CommonResponse<?>> getEmailExists(
            @Validated(OnSendEmail.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        // 1. 유효성 검사 실행
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 2.  이메일 암호화
        String email = CmmUtil.nvl(pDTO.userEmail()); // 회원 이메일
        String encryptEmail = EncryptUtil.encAES128CBC(pDTO.userEmail()); // 암호화된 회원이메일
        log.info("email: {}", email);
        log.info("encryptEmail: {}", encryptEmail);

        UserInfoDTO eDTO = UserInfoDTO.builder()
                .userEmail(encryptEmail)
                .build();

        // 이메일 존재 여부 확인
        ExistsYnDTO rDTO = Optional.ofNullable(userInfoService.getUserEmailExists(eDTO))
                .orElse(ExistsYnDTO.builder().build());
        log.info("rDTO: {}", rDTO);
        log.info("rDTO.existsYn: {}", rDTO.existsYn());

        // 이메일 존재 하면
        // 이메일은 정확하게 대소문자 구분해서 확인
        if ("Y".equals(rDTO.existsYn())) {

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "fail",
                            SweetAlertMsgDTO.fail("중복된 이메일", "이미 가입된 이메일입니다"))
            );
        }
        log.info("{}.getEmailExists End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "success",
                        SweetAlertMsgDTO.success("사용 가능", "해당 이메일로 인증번호가 전송됩니다."))
        );
    }




}
