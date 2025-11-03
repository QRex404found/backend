//QR 분석 요청 및 결과를 담습니다.
package com.found.qrex.dto;

import com.found.qrex.domain.Analysis;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class AnalysisDto {

    // =======================================================================
    // 1. RAG 통신용 DTO (FastAPI와 통신)
    // =======================================================================

    // FastAPI로 보낼 요청 DTO (AnalysisService에서 사용)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGAnalysisRequest {
        private String url;
        private String ip_location;
        private String safe_browsing_result;
    }

    // FastAPI에서 받을 응답 DTO (AnalysisService에서 사용)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGAnalysisResponse {
        // LLM의 최종 JSON 출력을 문자열로 받습니다.
        private String rag_json_result;
    }

    // =======================================================================
    // 2. 프론트엔드 응답 DTO (Controller에서 사용)
    // =======================================================================

    // 2-1. 특정 분석 결과 상세 응답 DTO
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisResultResponse {
        private Integer analysisId;
        private String url;

        // RAG 출력 필드
        private String riskLevel;
        private String reason;

        // RAG 입력/전처리 필드 (프론트엔드 상세 정보 표시용)
        private String ipLocation;
        private String safeBrowsingResult;
        private String ipAddress;

        // 사용자 입력 필드
        private String analysisTitle;

        // 엔티티 -> DTO 변환 팩토리 메서드
        public static AnalysisResultResponse fromEntity(Analysis analysis) {
            return AnalysisResultResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    .url(analysis.getAnalyzedUrl())
                    .riskLevel(analysis.getRiskLevel().toString())
                    .reason(analysis.getReason())
                    .ipLocation(analysis.getIpLocation())
                    .ipAddress(analysis.getIpAddress())
                    .safeBrowsingResult(analysis.getSafeBrowsingResult())
                    .analysisTitle(analysis.getAnalysisTitle())
                    .build();
        }
    }

    // 2-2. 분석 기록 목록 조회 DTO
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisHistoryResponse {
        private Integer analysisId;
        private String analysisUrl;
        private String riskLevel;
        private LocalDateTime createdAt;

        public static AnalysisHistoryResponse fromEntity(Analysis analysis) {
            return AnalysisHistoryResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    .analysisUrl(analysis.getAnalyzedUrl())
                    .riskLevel(analysis.getRiskLevel().toString())
                    .createdAt(analysis.getCreatedAt())
                    .build();
        }
    }

    // 2-3. 제목 수정 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTitleRequest {
        private String title;
    }
}