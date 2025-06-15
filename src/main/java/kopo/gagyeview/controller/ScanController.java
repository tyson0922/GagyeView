package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import kopo.gagyeview.controller.response.CommonResponse;
import kopo.gagyeview.dto.ScanRequestDTO;
import kopo.gagyeview.service.IScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/receipt")
public class ScanController {

    private final IScanService scanService;

    @GetMapping("")
    public String scanPage(Model model, HttpSession session) {
        log.info("✅ 영수증 스캔 페이지 접속");

        // ✅ 세션에서 사용자 ID 가져오기
        String userId = (String) session.getAttribute("SS_USER_ID");
        if (userId == null || userId.isBlank()) {
            return "redirect:/"; // redirect to home or login
        }
        // ✅ 모델에 사용자 ID 추가
        model.addAttribute("userId", userId);

        return "scan/scan";  // your scan.jsp or scan.html
    }

    /**
     * ✅ Google Vision OCR 요청 (Base64 이미지)
     */
    @PostMapping("/api/v1/vision")
    @ResponseBody
    public ResponseEntity<CommonResponse<?>> extractText(@RequestBody ScanRequestDTO pDTO) {
        log.info("📥 Vision OCR 요청");

        String base64Image = pDTO.getImage();
        String extractedText = scanService.callGoogleVisionAPI(base64Image);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "텍스트 추출 성공", extractedText)
        );
    }


    @PostMapping("/api/v1/analyze-and-save")
    @ResponseBody
    public ResponseEntity<CommonResponse<?>> analyzeAndSave(@RequestBody ScanRequestDTO pDTO) {
        log.info("📤 GPT 분석 및 저장 요청: userId = {}", pDTO.getUserId());

        try {
            // ✅ Analyze and save
            int result = scanService.analyzeAndSaveTransaction(pDTO.getOcrText(), pDTO.getUserId());

            if (result == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        CommonResponse.of(HttpStatus.BAD_REQUEST, "분석 또는 저장 실패", null)
                );
            }

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "분석 및 저장 성공", result)
            );

        } catch (Exception e) {
            log.error("❌ 분석 및 저장 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null)
            );
        }
    }
}
