package kopo.gagyeview.persistance.mapper;

import kopo.gagyeview.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserInfoMapper {

    // 회원 정보 등록하기(가입하기)
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    // 회원 가입 전 아이디 중복체크하기(DB 체크하기)
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    // 회원 가입 전 이메일 중복체크하기(DB 조회하기)(DB 조회하기)
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

}
