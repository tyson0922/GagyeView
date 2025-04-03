package kopo.gagyeview.service;

import kopo.gagyeview.dto.UserInfoDTO;

public interface IUserInfoService {

    // 아이디 중복 체크
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    // 이메일 주소 체크
    UserInfoDTO getUserEmailExists(UserInfoDTO pDTO) throws Exception;

    // 회원 가입하기(회원 정보 등록)
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;


}
