//QR 코드 분석, 기록 조회, 그리고 제목 변경과 관련된 API를 제공
package com.found.qrex.controller;

// import com.found.qrex.domain.Analysis; // 🌟 Analysis 엔티티 임포트 삭제 (DTO만 사용)
import com.found.qrex.domain.Analysis;
import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.dto.AnalysisDto.AnalysisResultResponse;
import com.found.qrex.dto.AnalysisDto.UpdateTitleRequest;
import com.found.qrex.service.AnalysisService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "분석 API", description = "QR 코드 분석, 기록 조회, 제목 변경 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/analyze")
    @Operation(summary = "URL 기반 QR 피싱 분석", description = "URL을 받아 GeoIP, SafeBrowsing을 거쳐 RAG 분석을 실행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분석 성공", content = @Content(schema = @Schema(implementation = AnalysisResultResponse.class))),
            @ApiResponse(responseCode = "500", description = "분석 서버(FastAPI) 오류 또는 내부 오류")
    })
    public ResponseEntity<AnalysisResultResponse> analyzeQr(@RequestParam(value = "url") String url) {
        try {
            // Service는 Analysis 엔티티를 반환합니다.
            Analysis analysisEntity = analysisService.scanAndAnalyze(url);
            // Analysis 엔티티를 DTO로 변환하여 반환
            return ResponseEntity.ok(AnalysisResultResponse.fromEntity(analysisEntity));
        } catch (Exception e) {
            System.err.println("QR 분석 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/history")
    @Operation(summary = "분석 기록 조회", description = "로그인된 사용자의 QR 코드 분석 기록을 페이지별로 조회합니다.")
    public ResponseEntity<Page<AnalysisDto.AnalysisHistoryResponse>> getAnalysisHistory(Pageable pageable) {

        // 🌟 오류 해결: Service가 DTO Page를 반환하므로, DTO Page 타입으로 받습니다.
        Page<AnalysisDto.AnalysisHistoryResponse> history = analysisService.getAnalysisHistory(pageable);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{analysisId}")
    @Operation(summary = "특정 분석 결과 조회", description = "특정 분석 ID에 해당하는 상세 결과를 조회합니다.")
    public ResponseEntity<AnalysisResultResponse> getAnalysisResult(@PathVariable Integer analysisId) {

        // 🌟 오류 해결: Service가 DTO를 반환하므로, DTO 타입으로 받습니다.
        AnalysisResultResponse result = analysisService.getAnalysisResult(analysisId);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/history/{analysisId}")
    @Operation(summary = "분석 기록 제목 수정", description = "특정 분석 ID에 해당하는 기록의 제목을 수정합니다.")
    public ResponseEntity<String> updateAnalysisTitle(@PathVariable Integer analysisId, @RequestBody UpdateTitleRequest request) {
        analysisService.updateAnalysisTitle(analysisId, request.getTitle());
        return ResponseEntity.ok("제목이 성공적으로 업데이트되었습니다.");
    }
}