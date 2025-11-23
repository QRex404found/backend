package com.found.qrex.controller;

import java.util.Map;

import com.found.qrex.domain.Analysis;
import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.dto.AnalysisDto.AnalysisResultResponse;
import com.found.qrex.dto.AnalysisDto.UpdateTitleRequest;
import com.found.qrex.service.AnalysisService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // 추가됨
import org.springframework.data.web.PageableDefault; // 추가됨
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "분석 API", description = "QR 코드 분석, 기록 조회, 제목 변경 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // [기존 기능 1] QR 분석
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResultResponse> analyzeQr(@RequestParam("url") String url) {
        try {
            Analysis analysis = analysisService.scanAndAnalyze(url);
            return ResponseEntity.ok(AnalysisResultResponse.fromEntity(analysis));
        } catch (Exception e) {
            System.err.println("QR 분석 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // [기존 기능 2] 내 기록 조회 (로그인 유저용)
    @GetMapping("/history")
    public ResponseEntity<Page<AnalysisDto.AnalysisHistoryResponse>> getAnalysisHistory(Pageable pageable) {
        Page<AnalysisDto.AnalysisHistoryResponse> history = analysisService.getAnalysisHistory(pageable);
        return ResponseEntity.ok(history);
    }

    // [기존 기능 3] 상세 조회
    @GetMapping("/history/{analysisId}")
    public ResponseEntity<AnalysisResultResponse> getAnalysisResult(@PathVariable Integer analysisId) {
        AnalysisResultResponse result = analysisService.getAnalysisResult(analysisId);
        return ResponseEntity.ok(result);
    }

    // [기존 기능 4] 제목 수정 (로그인 유저용)
    @PutMapping("/history/{analysisId}")
    public ResponseEntity<String> updateAnalysisTitle(
            @PathVariable Integer analysisId,
            @RequestBody UpdateTitleRequest request) {

        analysisService.updateAnalysisTitle(analysisId, request.getTitle());
        return ResponseEntity.ok("제목이 성공적으로 업데이트되었습니다.");
    }

    // =================================================================
    // 👇 [여기가 추가된 부분입니다] AI가 데이터를 읽을 수 있게 해주는 통로
    // =================================================================

    // ⭐️ [AI 전용] 기록 조회
    @GetMapping("/ai/history")
    public ResponseEntity<Page<AnalysisDto.AnalysisHistoryResponse>> getHistoryForAi(
            @RequestParam("writerId") String writerId,
            @PageableDefault(size = 10, sort = "CREATED_AT", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        System.out.println("📡 [Controller] AI가 기록 조회를 요청함! ID: " + writerId);
        return ResponseEntity.ok(analysisService.getHistoryByUserId(writerId, pageable));
    }

    // ⭐️ [AI 전용] 제목 수정
    @PatchMapping("/ai/title")
    public ResponseEntity<String> updateTitleForAi(@RequestBody Map<String, String> request) {

        // 🔹 raw 값들 그대로 안전하게 꺼내기
        String analysisIdRaw = request.get("analysisId");
        String newTitle = request.get("newTitle");
        String userId = request.get("userId");         // AI가 함께 보낼 수 있는 값으로 가정
        String analyzedUrl = request.get("analyzedUrl"); // 같은 URL 여러 번 분석했을 때 구분용

        // 🔹 analysisId 파싱 (null / "null" / "" 방어)
        Integer analysisId = null;
        if (analysisIdRaw != null) {
            String trimmed = analysisIdRaw.trim();
            if (!trimmed.isEmpty()
                    && !"null".equalsIgnoreCase(trimmed)
                    && !"undefined".equalsIgnoreCase(trimmed)) {
                try {
                    analysisId = Integer.valueOf(trimmed);
                } catch (NumberFormatException e) {
                    // 숫자 아니면 그냥 null로 두고 Service에서 최신 기록 찾게 함
                }
            }
        }

        System.out.println("📡 [Controller] AI가 제목 수정을 요청함! " +
                "analysisId=" + analysisId +
                ", userId=" + userId +
                ", url=" + analyzedUrl +
                ", newTitle=" + newTitle);

        // 🔥 핵심: Service 쪽에서
        // 1) analysisId 있으면 그걸로
        // 2) 없으면 (userId + analyzedUrl) 기준 최신 기록
        // 3) 그래도 없으면 userId 기준 최신 기록
        analysisService.updateTitleByAi(analysisId, analyzedUrl, userId, newTitle);

        return ResponseEntity.ok("AI에 의해 제목이 수정되었습니다.");
    }
}
