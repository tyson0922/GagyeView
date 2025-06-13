package kopo.gagyeview.persistence.mapper;

import kopo.gagyeview.dto.DefCatDTO;
import kopo.gagyeview.dto.UserCatDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ICatMapper {

    // DefCat table query
    int countDefCat();
    int insertDefCat(DefCatDTO pDTO);
    List<DefCatDTO> getDefaultCategories();
    int deleteUserCategories(String userId);


    // UserCat table query
    boolean existsUserCat(UserCatDTO pDTO);
    int insertUserCat(UserCatDTO pDTO);
    List<UserCatDTO> getUserCategories(String userId);

    // ✅ 사용자 카테고리 수정 및 삭제
    int updateUserCat(UserCatDTO pDTO);
    int deleteUserCat(UserCatDTO pDTO);
}
