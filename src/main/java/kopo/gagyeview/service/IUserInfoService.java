package kopo.gagyeview.service;

import kopo.gagyeview.dto.ExistsYnDTO;
import kopo.gagyeview.dto.UserInfoDTO;

public interface IUserInfoService {

    // 아이디 중복 체크
    ExistsYnDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    // 이메일 주소 체크
    ExistsYnDTO getUserEmailExists(UserInfoDTO pDTO) throws Exception;

    // 회원 가입하기(회원 정보 등록)
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    // 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
    UserInfoDTO getLogin(UserInfoDTO pDTO) throws Exception;

    // 아이디, 비밀번호 찾기에 활용
    UserInfoDTO getUserIdOrPw(UserInfoDTO pDTO) throws Exception;

    // 아이디 찾기 성공후 인증 이메일 보내기
    UserInfoDTO sendAuthCode(UserInfoDTO pDTO) throws Exception;

    // 비밀번호 재설정
    int newPwProc(UserInfoDTO pDTO) throws Exception;
}
