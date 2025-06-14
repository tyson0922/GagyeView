package kopo.gagyeview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(value="/finInfo")
public class FinInfoController {

    private final IFinInfoService finInfoService;

    @GetMapping(value = "")
    public String finInfoPage(@RequestParam(required = false) String yrMon,
                              HttpSession session,
                              Model model) {
        log.info("{}.finInfoPage Start!", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId == null || userId.isEmpty()) {
            log.info("userId is null or empty!");
            return "redirect:/";
        }

        if (yrMon == null || yrMon.isEmpty()) {
            LocalDate now = LocalDate.now();
            yrMon = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        log.info("Logged in userId: {}", userId);

        try {
            List<DonutChartDTO> donutExpenseData = finInfoService.getDonutByCatType(userId, "지출", yrMon);
            List<DonutChartDTO> donutIncomeData = finInfoService.getDonutByCatType(userId, "수입", yrMon);

            List<BarChartDTO> monthlyIncomeExpenseData = finInfoService.getMonthlyIncomeExpense(userId);
            List<StackBarDTO> monthlyExpenseStackData = finInfoService.getMonthlyStack(userId, "지출");
            List<StackBarDTO> monthlyIncomeStackData = finInfoService.getMonthlyStack(userId, "수입");

            BigDecimal totalExpense = finInfoService.getTotalAmountByType(userId, "지출");
            BigDecimal totalIncome = finInfoService.getTotalAmountByType(userId, "수입");

            BigDecimal monthlyExpense = finInfoService.getMonthlyTotal(userId, "지출", yrMon);
            BigDecimal monthlyIncome = finInfoService.getMonthlyTotal(userId, "수입", yrMon);

            log.info("📊 donutExpenseData (지출) 건수: {}", donutExpenseData.size());
            for (DonutChartDTO dto : donutExpenseData) {
                log.debug("   ↳ name: {}, value: {}", dto.getName(), dto.getValue());
            }

            log.info("📊 donutIncomeData (수입) 건수: {}", donutIncomeData.size());
            for (DonutChartDTO dto : donutIncomeData) {
                log.debug("   ↳ name: {}, value: {}", dto.getName(), dto.getValue());
            }

            log.info("📊 monthlyIncomeExpenseData 건수: {}", monthlyIncomeExpenseData.size());
            log.info("📊 monthlyExpenseStackData 건수: {}", monthlyExpenseStackData.size());
            log.info("📊 monthlyIncomeStackData 건수: {}", monthlyIncomeStackData.size());

            log.info("💰 총 지출 (totalExpense): {}", totalExpense);
            log.info("💰 총 수입 (totalIncome): {}", totalIncome);
            log.info("📅 월 지출 ({}): {}", yrMon, monthlyExpense);
            log.info("📅 월 수입 ({}): {}", yrMon, monthlyIncome);

            ObjectMapper objectMapper = new ObjectMapper();

            model.addAttribute("selectedYrMon", yrMon);

            model.addAttribute("donutExpenseJson", objectMapper.writeValueAsString(donutExpenseData));
            model.addAttribute("donutIncomeJson", objectMapper.writeValueAsString(donutIncomeData));
            model.addAttribute("monthlyIncomeExpenseJson", objectMapper.writeValueAsString(monthlyIncomeExpenseData));
            model.addAttribute("monthlyExpenseStackJson", objectMapper.writeValueAsString(monthlyExpenseStackData));
            model.addAttribute("monthlyIncomeStackJson", objectMapper.writeValueAsString(monthlyIncomeStackData));

            model.addAttribute("totalExpense", totalExpense);
            model.addAttribute("totalIncome", totalIncome);
            model.addAttribute("monthlyExpense", monthlyExpense);
            model.addAttribute("monthlyIncome", monthlyIncome);

        } catch (Exception e) {
            log.error("finInfoPage data load error", e);
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
