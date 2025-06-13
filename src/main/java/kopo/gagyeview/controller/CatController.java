package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.dto.UserCatDTO;
import kopo.gagyeview.service.ICatService;
import kopo.gagyeview.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cat")
public class CatController {

    private final ICatService catService;

    // 🧼 카테고리 초기화 (페이지 리다이렉션용)
    @PostMapping("/reset")
    public String resetCategories(HttpSession session) throws Exception {
        String userId = (String) session.getAttribute("SS_USER_ID");

        if (userId == null) {
            log.warn("⛔ 세션 없음: 로그인 필요");
            return "redirect:/login";
        }

        catService.resetUserCatToDefault(userId);
        return "redirect:/finInfo/catPage";
    }

    // 🧾 카테고리 목록 페이지 진입
    @GetMapping("/list")
    public String getUserCategories(HttpSession session, Model model) throws Exception {
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        if (userId.isEmpty()) return "redirect:/login";

        List<UserCatDTO> catList = catService.getUserCategories(userId);
        model.addAttribute("catList", catList);

        return "finInfo/catPage";
    }

    // ✅ [API] 카테고리 추가
    @ResponseBody
    @PostMapping("/insertUserCat")
    public ResponseEntity<? extends CommonResponse<?>> insertUserCat(
            @RequestBody UserCatDTO pDTO,
            HttpSession session
    ) throws Exception {
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.fail("등록 실패", "로그인이 필요합니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        pDTO.setUserId(userId);
        pDTO.setRegId(userId);
        pDTO.setChgId(userId);

        catService.insertUserCat(pDTO);
        SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.success("등록 성공", "카테고리가 등록되었습니다.");
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
    }

    // ✅ [API] 카테고리 수정
    @ResponseBody
    @PostMapping("/updateUserCat")
    public ResponseEntity<? extends CommonResponse<?>> updateUserCat(
            @RequestBody UserCatDTO pDTO,
            HttpSession session
    ) throws Exception {
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.fail("수정 실패", "로그인이 필요합니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        pDTO.setUserId(userId);
        pDTO.setChgId(userId);

        int res = catService.updateUserCat(pDTO);

        SweetAlertMsgDTO samDTO = (res > 0)
                ? SweetAlertMsgDTO.success("수정 성공", "카테고리가 수정되었습니다.")
                : SweetAlertMsgDTO.fail("수정 실패", "카테고리 수정에 실패했습니다.");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, res > 0 ? "success" : "fail", samDTO));
    }

    // ✅ [API] 카테고리 삭제
    @ResponseBody
    @PostMapping("/deleteUserCat")
    public ResponseEntity<? extends CommonResponse<?>> deleteUserCat(
            @RequestBody UserCatDTO pDTO,
            HttpSession session
    ) throws Exception {
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.fail("삭제 실패", "로그인이 필요합니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        pDTO.setUserId(userId);

        int res = catService.deleteUserCat(pDTO);

        SweetAlertMsgDTO samDTO = (res > 0)
                ? SweetAlertMsgDTO.success("삭제 성공", "카테고리가 삭제되었습니다.")
                : SweetAlertMsgDTO.fail("삭제 실패", "카테고리 삭제에 실패했습니다.");

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, res > 0 ? "success" : "fail", samDTO));
    }

    // ✅ [API] 카테고리 초기화 (AJAX용)
    @ResponseBody
    @PostMapping("/resetUserCats")
    public ResponseEntity<? extends CommonResponse<?>> resetUserCats(HttpSession session) throws Exception {
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.fail("초기화 실패", "로그인이 필요합니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        catService.resetUserCatToDefault(userId);
        SweetAlertMsgDTO samDTO = SweetAlertMsgDTO.success("초기화 성공", "기본 카테고리로 초기화되었습니다.");
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
    }
}