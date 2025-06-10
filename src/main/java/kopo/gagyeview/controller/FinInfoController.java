package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.AggregationResultDTO;
import kopo.gagyeview.dto.MonTrnsDTO;
import kopo.gagyeview.dto.MonTrnsDetailDTO;
import kopo.gagyeview.dto.SweetAlertMsgDTO;
import kopo.gagyeview.service.IFinInfoService;
import kopo.gagyeview.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(value="/finInfo")
public class FinInfoController {

    private final IFinInfoService finInfoService;

    @GetMapping(value="")
    public String finInfoPage(HttpSession session, Model model) {
        log.info("{}.finInfoPage Start!", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId == null || userId.isEmpty()) {
            log.info("userId is null or empty!");
            return "redirect:/";
        }

        log.info("Logged in userId: {}", userId);

        try {
            // 서비스 호출
            List<AggregationResultDTO> donutData = finInfoService.monTotalByType(userId);
            List<AggregationResultDTO> barData = finInfoService.monIncomeExpense(userId);
            List<AggregationResultDTO> stackData = finInfoService.monthlyCategoryStack(userId);

            log.info("donutData size: {}", donutData.size());
            log.info("barData size: {}", barData.size());
            log.info("stackData size: {}", stackData.size());

            // 모델에 데이터 담기
            model.addAttribute("donutData", donutData);
            model.addAttribute("barData", barData);
            model.addAttribute("stackData", stackData);

        } catch (Exception e) {
            log.error("finInfoPage data load error: {}", e.getMessage());
        }
        log.info("{}.finInfoPage End!", this.getClass().getName());


        return "finInfo/finInfo";
    }

    @GetMapping("/detail")
    public String finInfoPageDetail(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session,
            Model model) throws Exception {

        log.info("{}.finInfoPageDetail Start!", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        if (userId.isEmpty()) return "redirect:/";

        // Default to current month
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1).toString(); // 2025-05-01
            endDate = now.withDayOfMonth(now.lengthOfMonth()).toString(); // 2025-05-31
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        List<MonTrnsDTO> trnsList = finInfoService.getTrnsByDateRange(userId, startDate, endDate);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        formatter.setMaximumFractionDigits(0);

        List<Map<String, Object>> displayList = new ArrayList<>();
        for (MonTrnsDTO dto : trnsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("dto", dto);
            item.put("formattedTotNm", "₩" + formatter.format(dto.monTrnsDetailDTO().totNm()));
            displayList.add(item);
        }

        model.addAttribute("displayList", displayList);
        log.info("{}.finInfoPageDetail End!", this.getClass().getName());

        return "finInfo/finInfoDetail";
    }

    @GetMapping("/testInsert")
    @ResponseBody
    public String testInsertTransaction(HttpSession session) throws Exception {

        log.info("{}.testInsertTransaction Start!", this.getClass().getName());
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            return "세션 없음";
        }

        MonTrnsDetailDTO detailDTO = MonTrnsDetailDTO.builder()
                .catNm("식비")
                .trnsDt(new Date())
                .totNm(new BigDecimal("9000"))
                .srcNm("맥도날드")
                .note("빅맥")
                .build();

        MonTrnsDTO pDTO = MonTrnsDTO.builder()
                .userId(userId)
                .yrMon("2025-05")
                .catType("지출")
                .monTrnsDetailDTO(detailDTO)
                .regDt(new Date())
                .chgDt(new Date())
                .build();

        int result = finInfoService.insertTrns(pDTO);

        log.info("{}.testInsertTransaction End!", this.getClass().getName());

        return (result == 1) ? "Insert 성공" : "Insert 실패";
    }

    @PostMapping(value = "/manualInsert")
    @ResponseBody
    public ResponseEntity<? extends CommonResponse<?>> manualInsert(
            @RequestBody MonTrnsDTO reqDTO,
            HttpSession session
    ) throws Exception {

        log.info("{}.manualInsert Start!", this.getClass().getName());

        SweetAlertMsgDTO samDTO;
        ResponseEntity<? extends CommonResponse<?>> response;

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            if (userId.isEmpty()) {
                samDTO = SweetAlertMsgDTO.fail("입력 실패", "로그인 정보가 없습니다. 다시 로그인 해주세요.");
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
            }

            MonTrnsDetailDTO detailDTO = reqDTO.monTrnsDetailDTO();
            String catType = reqDTO.catType(); // "수입" or "지출"

            // 년월 추출 (예: 2025-05)
            Calendar cal = Calendar.getInstance();
            cal.setTime(detailDTO.trnsDt());
            String yrMon = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            MonTrnsDTO pDTO = MonTrnsDTO.builder()
                    .userId(userId)
                    .yrMon(yrMon)
                    .catType(catType)
                    .monTrnsDetailDTO(detailDTO)
                    .regDt(new Date())
                    .chgDt(new Date())
                    .build();

            int res = finInfoService.insertTrns(pDTO);

            if (res == 1) {
                samDTO = SweetAlertMsgDTO.success("입력 성공", "거래 내역이 성공적으로 등록되었습니다.");
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
            } else {
                samDTO = SweetAlertMsgDTO.fail("입력 실패", "저장에 실패했습니다.");
                response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
            }

        } catch (Exception e) {
            log.error("{}.manualInsert Error", this.getClass().getName(), e);
            samDTO = SweetAlertMsgDTO.fail("입력 실패", "서버 오류가 발생했습니다.");
            response = ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }

        log.info("{}.manualInsert End!", this.getClass().getName());
        return response;
    }


    @PostMapping("/modify")
    @ResponseBody
    public ResponseEntity<? extends CommonResponse<?>> modifyTrns(
            @RequestBody MonTrnsDTO pDTO,
            HttpSession session
    ) {
        log.info("{}.modifyTrns Start!", this.getClass().getName());

        SweetAlertMsgDTO samDTO;
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.isEmpty()) {
            samDTO = SweetAlertMsgDTO.fail("수정 실패", "로그인 정보가 없습니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.UNAUTHORIZED, "fail", samDTO));
        }

        MonTrnsDTO rDTO = MonTrnsDTO.builder()
                .id(pDTO.id()) // ⬅ make sure `id` exists in the record
                .userId(userId)
                .yrMon(pDTO.yrMon())
                .catType(pDTO.catType())
                .monTrnsDetailDTO(pDTO.monTrnsDetailDTO())
                .regDt(pDTO.regDt()) // if keeping original reg date
                .chgDt(new Date()) // update timestamp
                .build();

        try {
            int res = finInfoService.updateTrns(rDTO);

            if (res > 0) {
                samDTO = SweetAlertMsgDTO.success("수정 성공", "거래 정보가 수정되었습니다.");
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
            } else {
                samDTO = SweetAlertMsgDTO.fail("수정 실패", "일치하는 데이터가 없습니다.");
                return ResponseEntity.ok(CommonResponse.of(HttpStatus.NOT_FOUND, "fail", samDTO));
            }

        } catch (Exception e) {
            log.error("Update Error: ", e);
            samDTO = SweetAlertMsgDTO.fail("수정 실패", "서버 오류가 발생했습니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "fail", samDTO));
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<? extends CommonResponse<?>> deleteTrns(@PathVariable String id) throws Exception {
        log.info("{}.deleteTrns Start! id={}", this.getClass().getName(), id);

        SweetAlertMsgDTO samDTO;
        int res = finInfoService.deleteTrnsById(id);

        if (res > 0) {
            samDTO = SweetAlertMsgDTO.success("삭제 성공", "거래 정보가 삭제되었습니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "success", samDTO));
        } else {
            samDTO = SweetAlertMsgDTO.fail("삭제 실패", "일치하는 데이터가 없습니다.");
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.NOT_FOUND, "fail", samDTO));
        }
    }

}
