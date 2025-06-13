package kopo.gagyeview.persistence.mapper;

import kopo.gagyeview.dto.UserBnkDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IUserBnkMapper {

    int insertUserBank(UserBnkDTO pDTO);

    int checkDuplicate(UserBnkDTO pDTO); // Optional: Check if this bank already exists for the user

    List<UserBnkDTO> selectUserBankList(String userId);

    int deleteUserBank(UserBnkDTO pDTO) throws Exception;

    int updateUserBank(UserBnkDTO pDTO) throws Exception;
}