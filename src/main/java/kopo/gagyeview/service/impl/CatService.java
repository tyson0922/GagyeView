package kopo.gagyeview.service.impl;

import kopo.gagyeview.dto.DefCatDTO;
import kopo.gagyeview.dto.UserCatDTO;
import kopo.gagyeview.persistence.mapper.ICatMapper;
import kopo.gagyeview.service.ICatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CatService implements ICatService {

    private final ICatMapper catMapper;

    @Override
    @Transactional
    public void resetUserCatToDefault(String userId) throws Exception {
        log.info("🔁 사용자 카테고리 초기화 시작: {}", userId);

        catMapper.deleteUserCategories(userId);
        insertDefaultCatsForUser(userId);

        log.info("✅ 사용자 카테고리 초기화 완료: {}", userId);
    }

    @Override
    @Transactional
    public void initUserCatFromDefault(String userId) throws Exception {
        log.info("👤 회원가입 시 기본 카테고리 입력: {}", userId);

        insertDefaultCatsForUser(userId);

        log.info("✅ 기본 카테고리 입력 완료: {}", userId);
    }

    @Override
    public void syncUserCat(UserCatDTO pDTO) throws Exception {
        log.info("syncUserCat Start!");

        if (!catMapper.existsUserCat(pDTO)) {
            log.info("카테고리 없음 → 새로 추가: {}, {}", pDTO.getCatNm(), pDTO.getCatType());
            catMapper.insertUserCat(pDTO);
        } else {
            log.info("카테고리 이미 존재: {}, {}", pDTO.getCatNm(), pDTO.getCatType());
        }

        log.info("syncUserCat End!");
    }

    @Override
    public List<UserCatDTO> getUserCategories(String userId) throws Exception {
        log.info("사용자 카테고리 조회 - userId: {}", userId);
        return catMapper.getUserCategories(userId);
    }

    // 공통 로직: DEF_CAT → USER_CAT
    private void insertDefaultCatsForUser(String userId) throws Exception {
        List<DefCatDTO> defaultCats = catMapper.getDefaultCategories();

        for (DefCatDTO defCat : defaultCats) {
            UserCatDTO userCat = new UserCatDTO();
            userCat.setUserId(userId);
            userCat.setCatNm(defCat.getCatNm());
            userCat.setCatType(defCat.getCatType());
            userCat.setRegId(userId);
            userCat.setChgId(userId);

            catMapper.insertUserCat(userCat);
        }
    }

    @Override
    public int insertUserCat(UserCatDTO pDTO) throws Exception {
        return catMapper.insertUserCat(pDTO);
    }

    @Override
    public int updateUserCat(UserCatDTO pDTO) throws Exception {
        return catMapper.updateUserCat(pDTO);
    }

    @Override
    public int deleteUserCat(UserCatDTO pDTO) throws Exception {
        return catMapper.deleteUserCat(pDTO);
    }
}