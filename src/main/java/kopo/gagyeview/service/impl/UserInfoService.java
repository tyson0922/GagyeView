package kopo.gagyeview.service.impl;

import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.MailDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.persistence.mapper.IUserInfoMapper;
import kopo.gagyeview.service.IMailService;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
import kopo.gagyeview.util.DateUtil;
import kopo.gagyeview.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoService implements IUserInfoService {

    private final IUserInfoMapper userInfoMapper; // 회원관련 SQL 사용하기 위한  Mapper 가져오기

    private final IMailService mailService; // 메일 발송을 위한 MailService 자바 객체 가져오기

    @Override
    public ExistsYnDTO getUserIdExists(UserInfoDTO pDTO) throws Exception {
        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        // DB 아이디가 존재하는지 SQL 쿼리 실행
        ExistsYnDTO rDTO = userInfoMapper.getUserIdExists(pDTO);

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public ExistsYnDTO getUserEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserEmailExists Start!", this.getClass().getName());

        ExistsYnDTO rDTO = Optional.ofNullable(userInfoMapper.getEmailExists(pDTO)).orElse(ExistsYnDTO.builder().build());

        log.info("rDTO: {}", rDTO);

        if (CmmUtil.nvl(rDTO.getExistsYn()).equals("N")) {

            // 6자리 랜덤 숫자 생성하기 (Integer because of Validation check)
            Integer authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            log.info("authNumber: {}.", authNumber);

            // 인증번호 발송 로직
            MailDTO mDTO = MailDTO.builder()
                    .title("이메일 중복 확인 인증번호 발송 메일")
                    .text("인증번호는 " + authNumber + "입니다")
                    .toMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getUserEmail())))
                    .build();

            mailService.sendMail(mDTO);

            mDTO = null;
            rDTO.setAuthNumber(authNumber);
        }

        log.info("{}.getUserEmailExists End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public void saveAuthToSession(HttpSession session, UserInfoDTO pDTO) throws Exception {

        String rawEmail = CmmUtil.nvl(pDTO.getUserEmail());
        Integer authNumber = Integer.parseInt(Optional.ofNullable(pDTO.getAuthNumber()).orElse("0"));

        Long sentTime = (Long) session.getAttribute("authSentTime");

        if (sentTime != null && System.currentTimeMillis() - sentTime  > 5 * 60 * 1000){
            session.removeAttribute("authNumber");
            session.removeAttribute("authSentTime");
            log.info("Expired authNumber: {}", authNumber);
        }

        session.setAttribute("authEmail", rawEmail);
        session.setAttribute("authNumber", authNumber);
        session.setAttribute("authSentTime", System.currentTimeMillis());

        log.info("New authNumber saved to session: {}", authNumber);

    }


    @Override
    public int insertUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        //회원가입 성공 : 1, 아이디 중복으로인한 가입 취소: 2, 기타 에러 발생 : 0;
        int res;

        // 회원가입
        int rowNumInserted = userInfoMapper.insertUserInfo(pDTO);
        log.info("rowNumInserted: {}", rowNumInserted);

        // db에 데이터가 등록되었다면(회원가입 성공했다면...)
        if (rowNumInserted > 0) {
            res = 1;

            // 회원가입 축하 메일 발송, 회원정보화면에서 입력받은 이메일 변수(아직 암호화되어 넘어오기 때문에 복호화 수행함
            MailDTO mDTO = MailDTO.builder()
                    .title("회원가입 축하합니다.")
                    .text(CmmUtil.nvl(pDTO.getUserName()) + "님의 회원가입을 축하합니다.")
                    .toMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getUserEmail())))
                    .build();

            mailService.sendMail(mDTO);
        } else {
            res = 0;
        }
        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }

    /**
     * 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원아이디, 비밀번호
     * @return 로그인된 회원아이디 정보
     * @throws Exception
     */
    @Override
    public UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getLogin Start!", this.getClass().getName());

        //로그인을 위한 아이디와 비밀번호 일치하는지 확인하기위한 mapper 호출하기
        UserInfoDTO rDTO = Optional.ofNullable(userInfoMapper.getLogin(pDTO))
                .orElseGet(UserInfoDTO::new);

        /**
         * UserInfoMapper로 부터 select 쿼리의 결과로 회원아이디를 받아왔다면, 로그인 성공
         *
         * DTO의 변수의 값이 있는지 확인하기 처리속도 측면에서 가장 좋은 방법은 변수의 길이를 가져오는 것이다.
         * 따라서 .length() 함수를 통해 회원아이디의 글자수를 가져와 0보다 큰지 비교한다.
         * 0보다 크다면, 글자가 존재하는 것이기 때문에 값이 존재한다.
         */
        if (!CmmUtil.nvl(rDTO.getUserName()).isEmpty()) {


            // 아이디, 패스워드 일치하는지 체크하는 쿼리에서 이메일 값 가져옴.(아직 암호화 되어 있어서, 복호화 수행함)
            MailDTO mDTO = MailDTO.builder()
                    .toMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(rDTO.getUserEmail())))
                    .title("로그인 알립!")
                    .text(DateUtil.getDateTime("yyyy.MM.dd hh:mm:ss") + "에 "
                    + CmmUtil.nvl(rDTO.getUserName()) + "님이 로그인하였습니다.")
                    .build();

            // 메일 발송
            mailService.sendMail(mDTO);
        }

        log.info("{}.getLogin End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception {


        log.info("{}.getUserId Start!", this.getClass().getName());

        UserInfoDTO rDTO = userInfoMapper.getUserId(pDTO);

        log.info("rDTO: {}", rDTO);

        log.info("{}.getUserId End!", this.getClass().getName());

        return rDTO;
    }


}
