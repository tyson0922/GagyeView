package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.MailDTO;
import kopo.gagyeview.dto.UserInfoDTO;
import kopo.gagyeview.persistence.mapper.IUserInfoMapper;
import kopo.gagyeview.service.IMailService;
import kopo.gagyeview.service.IUserInfoService;
import kopo.gagyeview.util.CmmUtil;
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

        if (CmmUtil.nvl(rDTO.existsYn()).equals("N")){

            // 6자리 랜덤 숫자 생성하기 (Integer because of Validation check)
            Integer authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            log.info("authNumber: {}.", authNumber);

            // 인증번호 발송 로직
            MailDTO mDTO = MailDTO.builder()
                    .title("이메일 중복 확인 인증번호 발송 메일")
                    .text("인증번호는 " + authNumber + "입니다")
                    .toMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.userEmail())))
                    .build();

            mailService.sendMail(mDTO);

            mDTO = null;

        }

        log.info("{}.getUserEmailExists End!", this.getClass().getName());

        return rDTO;
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
        if (rowNumInserted > 0){
            res = 1;

            // 회원가입 축하 메일 발송, 회원정보화면에서 입력받은 이메일 변수(아직 암호화되어 넘어오기 때문에 복호화 수행함
            MailDTO mDTO = MailDTO.builder()
                    .title("회원가입 축하합니다.")
                    .text(CmmUtil.nvl(pDTO.userName()) + "님의 회원가입을 축하합니다.")
                    .toMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.userEmail())))
                    .build();

            mailService.sendMail(mDTO);
        } else {
            res = 0;
        }
        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }


}
