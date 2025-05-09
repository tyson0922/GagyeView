package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.EncryptUtil;
import kopo.gagyeview.validation.*;
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
    @GetMapping(value = "/signUp")
    public String signUp() {
        log.info("{}.signUp", this.getClass().getName());

        return "/user/signUp";
    }

    @GetMapping(value = "/login")
    public String login(HttpSession session) {
        log.info("{}.login", this.getClass().getName());

        // 세션에서 사용자 아이디 가져오기
        String userId = (String) session.getAttribute("SS_USER_ID");

        log.info("userId : {}", userId);
        // 이미 로그인된 경우 메인 페이지로 리다이렉트
        if (userId != null) {
            log.info("이미 로그인된 사용자입니다. index 페이지로 리다이렉트");
            return "redirect:/";
        }

        return "/user/login";
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


    @GetMapping(value = "/myPage")
    public String myPage(HttpSession session) {
        log.info("{}.myPage", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"), "");
        String verified = CmmUtil.nvl((String) session.getAttribute("SS_VERIFIED"), "");

        if (userId.isEmpty()) {
            return "redirect:/user/login";
        }
        if (!verified.equals("Y")) {
            return "redirect:/user/verifyWithPw";
        }
        return "/user/myPage";
    }

    @GetMapping(value = "/newPw")
    public String newPw(HttpSession session) {
        log.info("{}.newPw Start!", this.getClass().getName());

        String findPwUserId = (String) session.getAttribute("findPwUserId");
        log.info("findPwUserId: {}", findPwUserId);

        if (findPwUserId == null || findPwUserId.isEmpty()) {
            return "redirect:/user/findPw";
        }

        log.info("{}.findPw End!", this.getClass().getName());
        return "/user/newPw";
    }

    @GetMapping(value = "/verifyWithPw")
    String verifWithPw(HttpSession session) {
        log.info("{}.verifWithPw Start!", this.getClass().getName());

        String sessionUserId = (String) session.getAttribute("SS_USER_ID");
        log.info("sessionUserId : {}", sessionUserId);

        if (sessionUserId == null || sessionUserId.isEmpty()) {
            return "redirect:/";
        }

        log.info("{}.verifWithPw End!", this.getClass().getName());
        return "/user/verifyWithPw";
    }

    @GetMapping(value = "/logout")
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
            HttpSession session) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        // 1. 유효성 검사 실행
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 2.  이메일 암호화
        String email = CmmUtil.nvl(pDTO.getUserEmail()); // 회원 이메일
        String encryptEmail = EncryptUtil.encAES128CBC(pDTO.getUserEmail()); // 암호화된 회원이메일
        log.info("email: {}", email);
        log.info("encryptEmail: {}", encryptEmail);

        UserInfoDTO eDTO = UserInfoDTO.builder()
                .userEmail(encryptEmail)
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

        if (rDTO.getAuthNumber() != null) {
//            HttpSession session = request.getSession();
            Long sentTime = (Long) session.getAttribute("authSentTime");

            if (sentTime != null && System.currentTimeMillis() - sentTime > 5 * 60 * 1000) {
                session.removeAttribute("authNumber");
                session.removeAttribute("authSentTime");
                log.info("Expired authNumber removed from session.");
            }

            session.setAttribute("authEmail", email);
            session.setAttribute("authNumber", rDTO.getAuthNumber());
            session.setAttribute("authSentTime", System.currentTimeMillis());
            log.info("New authNumber saved to session: {}", rDTO.getAuthNumber());

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
            BindingResult bindingResult, HttpSession session) throws Exception {

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
    @PostMapping(value = "loginProc")
    public ResponseEntity<? extends CommonResponse<?>> loginProc(
            @Validated(OnLogin.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 로그인 처리 결과를 저장할 변수들
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
                samText = "아이디 또는 비밀번호가 올바르지 않습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", samDTO
                ));
            }
        } catch (Exception e) {

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

    @ResponseBody
    @PostMapping(value = "findUserIdOrPwProc")
    public ResponseEntity<? extends CommonResponse<?>> findUserIdProc(
            @Validated(OnVerifyAuth.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) throws Exception {

        log.info("{}.findUserIdProc Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 아이디 찾기 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        pDTO.setUserEmail(EncryptUtil.encAES128CBC(pDTO.getUserEmail()));

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdOrPw(pDTO))
                .orElseGet(UserInfoDTO::new);
        log.info("rDTO: {}", rDTO);

        if (!CmmUtil.nvl(rDTO.getUserId()).isEmpty()) { // 아이디 존재 성공시 인증번호 보냄

            rDTO = Optional.ofNullable(userInfoService.sendAuthCode(rDTO))
                    .orElseGet(UserInfoDTO::new);

            Long sentTime = (Long) session.getAttribute("authSentTime");

            if (sentTime != null && System.currentTimeMillis() - sentTime > 5 * 60 * 1000) {
                session.removeAttribute("authNumber");
                session.removeAttribute("authSentTime");
                session.removeAttribute("findPwUserId");
                log.info("Expired authNumber removed from session.");
            }

            session.setAttribute("authNumber", rDTO.getAuthNumber());
            session.setAttribute("authEmail", rDTO.getUserEmail());
            session.setAttribute("findPwUserId", rDTO.getUserId());
            log.info("authNumber: {} / authEmail: {}", rDTO.getAuthNumber(), rDTO.getUserEmail());
            log.info("findPwUserId : {}", rDTO.getUserId());

            samTitle = "메일 확인";
            samText = "해당 이메일로 인증번호가 전송됩니다.";
            samDTO = SweetAlertMsgDTO.success(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "success", samDTO
            ));

        } else {

            samTitle = "찾기 실패";
            samText = "아이디 또는 이메일이 올바르지 않습니다";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "fail", samDTO
            ));

        }

        return response;

    }

    @ResponseBody
    @PostMapping(value = "findUserIdResult")
    public ResponseEntity<? extends CommonResponse<?>> findUserIdResult(
            @Validated(OnFindIdOrPw.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) throws Exception {

        log.info("{}.findUserIdResult Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 아이디 찾기 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        String rawEmail = CmmUtil.nvl(pDTO.getUserEmail());
        String authNumber = CmmUtil.nvl(pDTO.getAuthNumber());
        String sesAuthEmail = (String) session.getAttribute("authEmail");
        String sesAuthNumber = (String) session.getAttribute("authNumber");

        log.info("rawEmail: {} / authNumber: {}", rawEmail, authNumber);
        log.info("sesAuthEmail: {} / sesAuthNumber: {}", sesAuthEmail, sesAuthNumber);


        if (rawEmail.equals(sesAuthEmail) && authNumber.equals(sesAuthNumber)) {

            // 인증번호로 인증 성공

            //세션값 제거

            session.removeAttribute("authNumber");
            session.removeAttribute("authEmail");

            // 이메일은 확인할때 DB는 암호화 되어있어서, 다시 암호화해서 조회
            pDTO.setUserEmail(EncryptUtil.encAES128CBC(pDTO.getUserEmail()));

            UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdOrPw(pDTO))
                    .orElseGet(UserInfoDTO::new);
            log.info("rDTO: {}", rDTO);

            samTitle = "아이디 결과";
            samText = rDTO.getUserName() + "님의 아이디는 " + rDTO.getUserId() + "입니다.";
            samDTO = SweetAlertMsgDTO.success(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "success", samDTO
            ));

        } else {

            // 인증번호로 인증 실패
            samTitle = "인증실패";
            samText = "인증번호가 틀렸습니다";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "fail", samDTO
            ));
        }

        return response;

    }


    @ResponseBody
    @PostMapping(value = "verifyAuthCodeOnly")
    public ResponseEntity<? extends CommonResponse<?>> verifyAuthCodeOnly(
            @Validated(OnFindIdOrPw.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) throws Exception {
        log.info("{}.verifyAuthCodeOnly Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        // 아이디 찾기 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        String rawEmail = CmmUtil.nvl(pDTO.getUserEmail());
        String authNumber = CmmUtil.nvl(pDTO.getAuthNumber());
        String sesAuthEmail = (String) session.getAttribute("authEmail");
        String sesAuthNumber = (String) session.getAttribute("authNumber");

        log.info("rawEmail: {} / authNumber: {}", rawEmail, authNumber);
        log.info("sesAuthEmail: {} / sesAuthNumber: {}", sesAuthEmail, sesAuthNumber);

        if (rawEmail.equals(sesAuthEmail) && authNumber.equals(sesAuthNumber)) {

            // 인증번호로 인증 성공

//            //세션값 제거
//
//            session.removeAttribute("authNumber");
//            session.removeAttribute("authEmail");

            // 이메일은 확인할때 DB는 암호화 되어있어서, 다시 암호화해서 조회
            pDTO.setUserEmail(EncryptUtil.encAES128CBC(pDTO.getUserEmail()));

            UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdOrPw(pDTO))
                    .orElseGet(UserInfoDTO::new);
            log.info("rDTO: {}", rDTO);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "success", "인증에 성공했습니다."
            ));

        } else {

            // 인증번호로 인증 실패
            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.OK, "fail", "인증번호가 틀렸습니다."
            ));

        }

        return response;
    }


    @ResponseBody
    @PostMapping(value = "newPwProc")
    public ResponseEntity<? extends CommonResponse<?>> newUserPw(
            @Validated(OnNewPw.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) throws Exception {
        log.info("{}.newUserPwProc Start!", this.getClass().getName());

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String findPwUserId = CmmUtil.nvl((String) session.getAttribute("findPwUserId"));
        String sessionUserId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userId = !findPwUserId.isEmpty() ? findPwUserId : sessionUserId;

        // 비밀번호 생성 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;


        if (!userId.isEmpty()) {

            pDTO.setUserId(userId);

            //  findPwUserId 기반인 경우만 제거 (로그인 상태는 유지)
            if (!findPwUserId.isEmpty()) {
                session.removeAttribute("findPwUserId");
                session.removeAttribute("authNumber");
                session.removeAttribute("authEmail");
            }


            int result = userInfoService.newPwProc(pDTO); // returns 1 if success

            if (result == 1) {

                samTitle = "변경 성공";
                samText = "비밀번호가 성공적으로 변경되었습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "success", samDTO
                ));
            } else {

                samTitle = "변경 실패";
                samText = "비밀번호 변경 중 오류가 발생했습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "fail", samDTO
                ));
            }

        } else {

            samTitle = "세션 만료";
            samText = "세션이 만료되었습니다. 이메일 인증을 다시 진행해주세요.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(
                    HttpStatus.UNAUTHORIZED, "fail", samDTO
            ));

        }

        return response;
    }

    @ResponseBody
    @PostMapping(value = "/getUserPwCheckProc")
    public ResponseEntity<? extends CommonResponse<?>> getUserPwCheckProc(
            @Validated(OnPwCheck.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session
    ) throws Exception {
        log.info("{}.getUserPwCheckProc Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userPw = CmmUtil.nvl(pDTO.getUserPw());
        log.info("userId: {} / userPw: {}", userId, userPw);


        UserInfoDTO rDTO = UserInfoDTO.builder()
                .userId(userId)
                .userPw(userPw)
                .build();

        // 비밀번호 생성 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {

            if (userId.isEmpty()) {

                samTitle = "세션 만료";
                samText = "세션이 만료되었습니다. 이메일 인증을 다시 진행해주세요.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

                return ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.UNAUTHORIZED, "fail", samDTO
                ));
            }

            int result = Optional.ofNullable(userInfoService.getUserPwCheck(rDTO))
                    .orElse(0);
            log.info("result : {}", result);

            if (result == 1) {

                session.setAttribute("SS_VERIFIED", "Y");
                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "success", "비밀번호 확인 성공"
                ));
            } else {
                samTitle = "비밀번호 확인 실패";
                samText = "비밀번호가 틀렸습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "fail", samDTO));
            }

        } catch (Exception e) {

            log.error("{}.getUserPwCheckProc Error", this.getClass().getName(), e);
            samTitle = "시스템 오류";
            samText = "비밀번호 확인 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        return response;
    }

    @ResponseBody
    @PostMapping(value = "updateUserName")
    public ResponseEntity<? extends CommonResponse<?>> updateUserName(
            @Validated(OnUpdateUserName.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session
    ) throws Exception {
        log.info("{}.updateUserName Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }


        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userName = CmmUtil.nvl(pDTO.getUserName());
        log.info("userId: {} / userName: {}", userId, userName);

        // 비밀번호 생성 결과를 저장할 변수들
        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        if (userId.isEmpty()) {
            samTitle = "세션 만료";
            samText = "세션이 만료되었습니다. 다시 로그인해주세요.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        // 서비스에 보낼 dto 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .build();

        try {

            int result = userInfoService.updateUserName(
                    Optional.ofNullable(rDTO).orElseGet(UserInfoDTO::new));
            log.info("result : {}", result);

            if (result == 1) {
                // 이름 업데이트 성공
                samTitle = "이름 수정 성공";
                samText = "이름이 성공적으로 수정되었습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.OK, "success", samDTO
                ));
            } else {
                // 이름 업데이트 실패
                samTitle = "이름 수정 실패";
                samText = "이름 수정 중 문제가 발생했습니다. 다시 시도해주세요.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO
                ));
            }


        } catch (Exception e) {

            log.error("{}.getUserPwCheckProc Error", this.getClass().getName(), e);
            samTitle = "시스템 오류";
            samText = "이름 수정 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        return response;
    }

    @ResponseBody
    @PostMapping("updateUserEmail")
    public ResponseEntity<? extends CommonResponse<?>> updateUserEmail(
            @Validated(OnUpdateEmail.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session) {
        log.info("{}.updateUserEmail Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userEmail = CmmUtil.nvl(pDTO.getUserEmail());

        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {
            // 세션 시간으로 만료
            Long sentTime = (Long) session.getAttribute("authSentTime");
            if (sentTime == null || System.currentTimeMillis() - sentTime > 5 * 60 * 1000) {
                session.removeAttribute("authEmail");
                session.removeAttribute("authNumber");
                session.removeAttribute("authSentTime");

                samTitle = "세션 만료";
                samText = "인증 시간이 만료되었습니다. 다시 인증해주세요.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
            }

            // 세션 인증 확인
            String sessionEmail = (String) session.getAttribute("authEmail");
            if (sessionEmail == null || !userEmail.equals(sessionEmail)) {
                samTitle = "인증 오류";
                samText = "인증된 이메일이 아니거나 세션이 만료되었습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
            }

            // 세션 인증 통과 후 세션 삭제
            session.removeAttribute("authEmail");
            session.removeAttribute("authNumber");
            session.removeAttribute("authSentTime");

            // 서비스에 전달
            UserInfoDTO rDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .build();

            int result = userInfoService.updateUserEmail(rDTO);
            log.info("result : {}", result);

            if (result == 1) {
                samTitle = "이메일 변경 성공";
                samText = "이메일이 성공적으로 변경되었습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
            } else {
                samTitle = "이메일 변경 실패";
                samText = "이메일 변경 중 문제가 발생했습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
            }

        } catch (Exception e) {
            log.error("{}.updateUserEmail Error", this.getClass().getName(), e);
            samTitle = "시스템 오류";
            samText = "이메일 변경 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        return response;

    }

    @ResponseBody
    @PostMapping(value = "sendAuthEmailForEmailUpdate")
    public ResponseEntity<? extends CommonResponse<?>> sendAuthEmailForEmailUpdate(
            @Validated(OnSendEmail.class) @RequestBody UserInfoDTO pDTO,
            BindingResult bindingResult, HttpSession session
    ) throws Exception {

        log.info("{}.sendAuthEmailForEmailUpdate Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getFirstErrorAlert(bindingResult);
        }

        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {
            // 암호화된 이메일로 변환
            pDTO.setUserEmail(EncryptUtil.encAES128CBC(pDTO.getUserEmail()));

            // 인증코드 전송 로직
            UserInfoDTO rDTO = Optional.ofNullable(userInfoService.sendAuthCode(pDTO))
                    .orElseGet(UserInfoDTO::new);

            // 이전 인증번호가 있다면 만료 시간 체크 후 제거
            Long sentTime = (Long) session.getAttribute("authSentTime");

            if (sentTime != null && System.currentTimeMillis() - sentTime > 5 * 60 * 1000) {
                session.removeAttribute("authNumber");
                session.removeAttribute("authSentTime");
                session.removeAttribute("authEmail");
                log.info("Expired authNumber removed from session.");
            }

            // 새 인증번호 저장
            session.setAttribute("authNumber", rDTO.getAuthNumber());
            session.setAttribute("authEmail", rDTO.getUserEmail());
            session.setAttribute("authSentTime", System.currentTimeMillis());

            log.info("authNumber: {} / authEmail: {}", rDTO.getAuthNumber(), rDTO.getUserEmail());

            samTitle = "메일 전송 완료";
            samText = "입력한 이메일 주소로 인증번호가 전송되었습니다.";
            samDTO = SweetAlertMsgDTO.success(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));

        } catch (Exception e) {
            log.error("{}.sendAuthEmailForEmailUpdate Error", this.getClass().getName(), e);

            samTitle = "메일 전송 실패";
            samText = "시스템 오류로 인증메일 전송에 실패했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);

            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        return response;
    }

    @ResponseBody
    @PostMapping(value="deleteUserById")
    public ResponseEntity<? extends CommonResponse<?>> deleteUserById(HttpSession session) throws Exception {
        log.info("{}.deleteUserById Start!", this.getClass().getName());

        String UserId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));


        String samTitle = "";
        String samText = "";
        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {


            if (UserId.isEmpty()) {
                samTitle = "세션 만료";
                samText = "세션이 만료되었습니다. 로그인을 다시 진행해주세요";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
            }

            UserInfoDTO pDTO = Optional.ofNullable(
                            UserInfoDTO.builder().userId(UserId).build())
                    .orElseGet(UserInfoDTO::new);

            int result = userInfoService.deleteUserById(pDTO);
            log.info("result : {}", result);

            if (result == 1) {
                session.invalidate();

                samTitle = "회원 탈퇴 성공";
                samText = "회원 탈퇴가 정상적으로 처리되었습니다.";
                samDTO = SweetAlertMsgDTO.success(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
            } else {
                samTitle = "회원 탈퇴 실패";
                samText = "회원 탈퇴 처리 중 문제가 발생했습니다.";
                samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
            }

        } catch (Exception e) {
            log.error("{}.deleteUserById Error", this.getClass().getName(), e);
            samTitle = "시스템 오류";
            samText = "회원 탈퇴 처리 중 오류가 발생했습니다.";
            samDTO = SweetAlertMsgDTO.fail(samTitle, samText);
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));

        } finally {
            log.info("samTitle: {} samText: {}", samTitle, samText);
            log.info("{}.deleteUserById End!", this.getClass().getName());
        }

        return response;
    }

}

