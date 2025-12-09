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


}
