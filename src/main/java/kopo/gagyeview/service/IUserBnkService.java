package kopo.gagyeview.service;

import kopo.gagyeview.dto.UserBnkDTO;

import java.util.List;

public interface IUserBnkService {
    int insertUserBank(UserBnkDTO pDTO) throws Exception;

    List<UserBnkDTO> getUserBankList(String userId) throws Exception;

    int deleteUserBank(UserBnkDTO pDTO) throws Exception;

    int updateUserBank(UserBnkDTO pDTO) throws Exception;
}
