package kopo.gagyeview.service;


import kopo.gagyeview.dto.UserCatDTO;

import java.util.List;

public interface ICatService {
    void resetUserCatToDefault(String userId) throws Exception;

    void initUserCatFromDefault(String userId) throws Exception; // For signup

    /**
     * 사용자 카테고리 존재 여부를 확인 후 없으면 추가
     * @param pDTO 사용자 카테고리 정보
     */
    void syncUserCat(UserCatDTO pDTO) throws Exception;

    List<UserCatDTO> getUserCategories(String userId) throws Exception;

    int insertUserCat(UserCatDTO pDTO) throws Exception;

    int updateUserCat(UserCatDTO pDTO) throws Exception;

    int deleteUserCat(UserCatDTO pDTO) throws Exception;
}