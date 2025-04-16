package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.EncryptUtil;
import kopo.gagyeview.validation.OnCheckUserId;
import kopo.gagyeview.validation.OnLogin;
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

        return "register";
    }

    @GetMapping(value = "/login")
    public String loginForm(HttpSession session) {
        log.info("{}.loginForm", this.getClass().getName());

        // 세션에서 사용자 아이디 가져오기
        String userId = (String) session.getAttribute("SS_USER_ID");

        log.info("userId : {}", userId);
        // 이미 로그인된 경우 메인 페이지로 리다이렉트
        if (userId != null) {
            log.info("이미 로그인된 사용자입니다. index 페이지로 리다이렉트");
            return "redirect:/";
        }



        return "login";
    }

    @GetMapping(value = "/findId")
    public String findId() {
        log.info("{}.findId", this.getClass().getName());
        return "/user/findId";
    }

    @GetMapping(value = "/findPw")
    public String findPw() {
        log.info("{}.findPw", this.getClass().getName());
        return "/user/findPw";
    }

    @GetMapping(value = "/newPw")
    public String newPw() {
        log.info("{}.newPw", this.getClass().getName());
        return "/user/newPw";
    }

    @GetMapping(value = "/myPage")
    public String myPage() {
        log.info("{}.myPage", this.getClass().getName());
        return "/user/myPage";
    }

    @GetMapping(value="/logout")
    public String logout(HttpSession session) {

        log.info("{}.logout Start!", this.getClass().getName());

        // 전체 세션 무효화(모든 값 제거)
        session.invalidate();
        log.info("세션 무효화됨, 로그아웃 완료");
        log.info("{}.logout End!", this.getClass().getName());

        return "redirect:/";
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
        String userId = CmmUtil.nvl(pDTO.getUserId());

        log.info("{}.userId received: {}", this.getClass().getName(), userId);

        // 회원아이디를 통해 중복된 아이디인지 조회
        ExistsYnDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElse(ExistsYnDTO.builder().build());
        log.info("rDTO: {}", rDTO);
        log.info("rDTO.existsYn : {}", rDTO.getExistsYn());

        // 아이디는 소분자 대문자 구분 안하기
        if ("Y".equalsIgnoreCase(rDTO.getExistsYn())) {
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
            BindingResult bindingResult,
            HttpServletRequest request) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        // 1. 유효성 검사 실행
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 2.  이메일 암호화
        String email = CmmUtil.nvl(pDTO.getUserEmail()); // 회원 이메일
        String encryptEmail = EncryptUtil.encAES128CBC(pDTO.getUserEmail()); // 암호화된 회원이메일
        String authNumber = pDTO.getAuthNumber();
        log.info("email: {}", email);
        log.info("encryptEmail: {}", encryptEmail);
        log.info("authNumber: {}", pDTO.getAuthNumber());

        UserInfoDTO eDTO = UserInfoDTO.builder()
                .userEmail(encryptEmail)
                .authNumber(authNumber)
                .build();

        // 이메일 존재 여부 확인
        ExistsYnDTO rDTO = Optional.ofNullable(userInfoService.getUserEmailExists(eDTO))
                .orElse(ExistsYnDTO.builder().build());
        log.info("rDTO: {}", rDTO);
        log.info("rDTO.existsYn: {}", rDTO.getExistsYn());

        // 이메일 존재 하면
        // 이메일은 정확하게 대소문자 구분해서 확인
        if ("Y".equals(rDTO.getExistsYn())) {

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "fail",
                            SweetAlertMsgDTO.fail("중복된 이메일", "이미 가입된 이메일입니다"))
            );
        }

        userInfoService.saveAuthToSession(request.getSession(), eDTO);
//        if (rDTO.getAuthNumber() != null) {
//            HttpSession session = request.getSession();
//            Long sentTime = (Long) session.getAttribute("authSentTime");
//
//            if (sentTime != null && System.currentTimeMillis() - sentTime > 5 * 60 * 1000) {
//                session.removeAttribute("authNumber");
//                session.removeAttribute("authSentTime");
//                log.info("Expired authNumber removed from session.");
//            }
//
//            session.setAttribute("authEmail", email);
//            session.setAttribute("authNumber", rDTO.getAuthNumber());
//            session.setAttribute("authSentTime", System.currentTimeMillis());
//            log.info("New authNumber saved to session: {}", rDTO.getAuthNumber());
//
//        }

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
            BindingResult bindingResult, HttpServletRequest request) throws Exception {

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
            String userId = CmmUtil.nvl(pDTO.getUserId());
            String userName = CmmUtil.nvl(pDTO.getUserName());
            String userEmail = CmmUtil.nvl(pDTO.getUserEmail());
            String userPw = CmmUtil.nvl(pDTO.getUserPw());


            log.info("userId: {}", userId);
            log.info("userName: {}", userName);
            log.info("userEmail: {}", userEmail);
            log.info("userPw: {}", userPw);


            // 이메일 인증 체크
            HttpSession session = request.getSession();
            String inputAuthCode = CmmUtil.nvl(pDTO.getAuthNumber());
            Integer savedCode = (Integer) session.getAttribute("authNumber");
            String savedEmail = (String) session.getAttribute("authEmail");

            if (savedCode == null || savedEmail == null || !userEmail.equals(savedEmail)) {
                samTitle = "이메일 인증 필요";
                samText = "이메일 인증을 진행해주세요.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.BAD_REQUEST, "fail", samDTO));
            }

            if (!inputAuthCode.equals(savedCode.toString())) {
                samTitle = "인증 실패";
                samText = "인증번호가 일치하지 않습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.BAD_REQUEST, "fail", samDTO));
            }

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

            if ("Y".equals(idDTO.getExistsYn()) || "Y".equals(emailDTO.getExistsYn())) {

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

                session.removeAttribute("verifiedEmail");
                session.removeAttribute("authEmail");

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
            log.info("samTitle: {} samText: {}", samTitle, samText);
            log.info("{}.insertUserInfo End!", this.getClass().getName());
        }
        return response;
    }

    /**
     * 로그인 처리 및 결과 알려주는 화면으로 이동
     */
    @ResponseBody
    @PostMapping(value="loginProc")
    public ResponseEntity<? extends CommonResponse<?>> loginProc(
            @Validated(OnLogin.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 로그인 처리 결과를 저장할 변수들
        int result = 0;
        String samIcon = "";
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {

            // 웹에서 받은 회원 아이디와 비밀번호
            String userId = CmmUtil.nvl(pDTO.getUserId());
            String userPw = CmmUtil.nvl(pDTO.getUserPw());

            log.info("{}.received login attempt for userId: {}", this.getClass().getName(), userId);

            // 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
            pDTO.setUserPw(EncryptUtil.encHashSHA256(userPw));

            // 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 userInfoService 호출하기
            UserInfoDTO rDTO = userInfoService.getLogin(pDTO);
            String userName = rDTO.getUserName();

            if (!CmmUtil.nvl(rDTO.getUserId()).isEmpty()) { // 로그인 성공

                samTitle = "환영합니다";
                samText = userName + "님, 로그인에 성공하셨습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "success", samDTO
                ));

                session.setAttribute("SS_USER_ID", userId);
                session.setAttribute("SS_USER_NAME", rDTO.getUserName());


            } else {

                samTitle = "로그인 실패";
                samText = "아이디와 비밀번호가 일치하지 않습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", samDTO
                ));
            }
        } catch (Exception e){

            //로그인이 실패되면 사용자에게 보여줄 메시지
            samTitle = "시스템 오류";
            samText = "로그인 처리 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO
            ));

            log.info("{}.loginProc Error", this.getClass().getName(), e);
        } finally {
            log.info("samTitle: {} samText: {}", samTitle, samText);
            log.info("{}.loginProc End!", this.getClass().getName());
        }

        return response;

    }




}

