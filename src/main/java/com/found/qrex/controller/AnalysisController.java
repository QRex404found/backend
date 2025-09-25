//QR 코드 분석, 기록 조회, 그리고 제목 변경과 관련된 API를 제공
package com.found.qrex.controller;

import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.service.AnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "Analysis Controller", description = "QR 코드 분석, 기록 조회, 제목 변경 관련 API")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "QR 코드 이미지 분석", description = "이미지 파일을 받아 QR 코드를 분석하고 결과를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분석 성공", content = @Content(schema = @Schema(implementation = AnalysisDto.AnalysisResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미지 파일이 없거나 형식 오류)")
    })
    public ResponseEntity<AnalysisDto.AnalysisResultResponse> scanQr(
            @RequestPart(value = "image") MultipartFile image) {
        AnalysisDto.AnalysisResultResponse result = analysisService.scanAndAnalyze(image);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    @Operation(summary = "분석 기록 조회", description = "로그인된 사용자의 QR 코드 분석 기록을 페이지별로 조회합니다.")
    public ResponseEntity<Page<AnalysisDto.AnalysisHistoryResponse>> getAnalysisHistory(Pageable pageable) {
        Page<AnalysisDto.AnalysisHistoryResponse> history = analysisService.getAnalysisHistory(pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{analysisId}")
    @Operation(summary = "특정 분석 결과 조회", description = "특정 분석 ID에 해당하는 상세 결과를 조회합니다.")
    public ResponseEntity<AnalysisDto.AnalysisResultResponse> getAnalysisResult(@PathVariable Integer analysisId) {
        AnalysisDto.AnalysisResultResponse result = analysisService.getAnalysisResult(analysisId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/history/{analysisId}")
    @Operation(summary = "분석 기록 제목 수정", description = "특정 분석 ID에 해당하는 기록의 제목을 수정합니다.")
    public ResponseEntity<String> updateAnalysisTitle(@PathVariable Integer analysisId, @RequestBody AnalysisDto.UpdateTitleRequest request) {
        analysisService.updateAnalysisTitle(analysisId, request.getTitle());
        return ResponseEntity.ok("제목이 성공적으로 업데이트되었습니다.");
    }
}