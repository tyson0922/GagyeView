package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.UserBnkDTO;
import kopo.gagyeview.persistence.mapper.IUserBnkMapper;
import kopo.gagyeview.service.IUserBnkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBnkService implements IUserBnkService {

    private final IUserBnkMapper userBnkMapper;

    @Override
    public int insertUserBank(UserBnkDTO pDTO) throws Exception {
        log.info("Insert new bank account: {}", pDTO);

        int exists = userBnkMapper.checkDuplicate(pDTO);
        if (exists > 0) {
            log.warn("Duplicate entry for userId={}, bnkNm={}", pDTO.getUserId(), pDTO.getBnkNm());
            return 0; // or throw exception
        }

        return userBnkMapper.insertUserBank(pDTO);
    }

    @Override
    public List<UserBnkDTO> getUserBankList(String userId) throws Exception {
        return userBnkMapper.selectUserBankList(userId);
    }
}

