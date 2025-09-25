//QR 코드 분석, 기록 조회, 그리고 제목 변경과 관련된 API를 제공
package com.found.qrex.controller;

import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.service.AnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/scan")
    public ResponseEntity<AnalysisDto.AnalysisResultResponse> scanQr(@RequestParam("image") MultipartFile image) {
        AnalysisDto.AnalysisResultResponse result = analysisService.scanAndAnalyze(image);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AnalysisDto.AnalysisHistoryResponse>> getAnalysisHistory(Pageable pageable) {
        Page<AnalysisDto.AnalysisHistoryResponse> history = analysisService.getAnalysisHistory(pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{analysisId}")
    public ResponseEntity<AnalysisDto.AnalysisResultResponse> getAnalysisResult(@PathVariable Integer analysisId) {
        AnalysisDto.AnalysisResultResponse result = analysisService.getAnalysisResult(analysisId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/history/{analysisId}")
    public ResponseEntity<String> updateAnalysisTitle(@PathVariable Integer analysisId, @RequestBody AnalysisDto.UpdateTitleRequest request) {
        analysisService.updateAnalysisTitle(analysisId, request.getTitle());
        return ResponseEntity.ok("제목이 성공적으로 업데이트되었습니다.");
    }
}