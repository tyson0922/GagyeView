package kopo.gagyeview.controller;

import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.EncryptUtil;
import kopo.gagyeview.validation.OnCheckUserId;
import kopo.gagyeview.validation.OnRegister;
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
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    private final IUserInfoService userInfoService;

    /**
     * 회원가입 화면으로 이동
     */
    @GetMapping(value = "/userRegForm")
    public String userRegForm() {
        log.info("{}.userRegForm", this.getClass().getName());

        return "/user/userRegForm";
    }

    /**
     * 회원 가입 전 아이디 중복체크하기( AJAX를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value = "/getUserIdExists")
    public ResponseEntity<? extends CommonResponse<?>> getUserIdExists(
            @Validated(OnCheckUserId.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        // DTO 유효성 검사 실패 시 에러 응답 반환
        if (bindingResult.hasErrors()) { // Spring Validation 맞춰 잘 바인딩 되었는지 체크
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 회원 아이디
        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("{}.userId received: {}", this.getClass().getName(), userId);

        // 회원아이디를 통해 중복된 아이디인지 조회
        ExistsYnDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElse(ExistsYnDTO.builder().build());
        log.info("rDTO: {}", rDTO);
        log.info("rDTO.existsYn : {}", rDTO.existsYn());

        // 아이디는 소분자 대문자 구분 안하기
        if ("Y".equalsIgnoreCase(rDTO.existsYn())) {
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
    @PostMapping(value = "getEmailExists")
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

    /**
     * 회원가입 로직 처리
     */
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public ResponseEntity<? extends CommonResponse<?>> insertUserInfo(
            @Validated(OnRegister.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 회원가입 결과
        int result = 0;

        //결과를 보내기 위해 SweetAlertMsgDTO 생성
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {

            /**
             * 웹(회원 정보 입력화면)에서 받는 정보를  String 변수에 저장 시작
             * 무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장함
             */
            String userId = CmmUtil.nvl(pDTO.userId());
            String userName = CmmUtil.nvl(pDTO.userName());
            String userEmail = CmmUtil.nvl(pDTO.userEmail());
            String userPw = CmmUtil.nvl(pDTO.userPw());

            log.info("userId: {}", userId);
            log.info("userName: {}", userName);
            log.info("userEmail: {}", userEmail);
            log.info("userPw: {}", userPw);

            pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .userName(userName)
                    .userEmail(EncryptUtil.encAES128CBC(userEmail))
                    .userPw(EncryptUtil.encHashSHA256(userPw))
                    .build();

            // 아이디 이메일 중복 확인
            ExistsYnDTO idDTO = userInfoService.getUserIdExists(pDTO);
            ExistsYnDTO emailDTO = userInfoService.getUserEmailExists(pDTO);
            log.info("idDTO: {} / emailDTO: {}", idDTO, emailDTO);

            if ("Y".equals(idDTO.existsYn()) || "Y".equals(emailDTO.existsYn())) {

                samTitle = "아이디 또는 이메일 중복.";
                samText = "아이디 또는 이메일이 중복합니다.";

                // SweetAlertMsgDTO 생성(record 안에 있는 method 사용)
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                log.info("samDTO: {}", samDTO);

                // 에러 결과 보내기 준비
                return ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", samDTO));
            }

            result = userInfoService.insertUserInfo(pDTO);
            log.info("회원 가입 result: {}", result);

            if (result == 1) {

                samTitle = "회원가입 성공";
                samText = "회원가입이 완료되었습니다. 로그인해주세요.";

                //SweetAlertMsgDTO 생성(record 안에 있는 method 사용)
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);
                log.info("samDTO: {}", samDTO);

                // 결과 메시지 보내기 준비
                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "success", samDTO));

            } else {
                samTitle = "회원가입 실패";
                samText = "오류로 인해 회원가입이 실패하였습니다.";

                //SweetAlertMsgDTO 생성(record 안에 있는 method 사용)
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                log.info("samDTO: {}", samDTO);

                // 에러 메시지 보내기
                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", samDTO
                ));
            }


        } catch (Exception e) {
            //저장이 실패되면 사용자에게 보여줄 메시지
            samTitle = "시스템 오류";
            samText = "회원가입 처리 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO
            ));

            log.info("{}.insertUserInfo Error", this.getClass().getName(), e);

        } finally {
            log.info("samTitle: {} samText: {}",samTitle,samText);
            log.info("{}.insertUserInfo End!", this.getClass().getName());
        }
        return response;
    }



}
