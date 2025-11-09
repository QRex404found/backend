package com.found.qrex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.RiskLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // ★ 1. 날짜 포맷을 위해 임포트

public class AnalysisDto {

    // (이하 RAG, Title, Result DTO는 기존 코드와 동일할 것입니다)

    // 1. RAG 분석 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGAnalysisRequest {
        private String url;
        @JsonProperty("ip_location") // (Python 서버와 키 이름 맞춤)
        private String ipLocation;
        @JsonProperty("safe_browsing") // (Python 서버와 키 이름 맞춤)
        private String safeBrowsing;
    }

    // 2. RAG 분석 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGAnalysisResponse {
        private String rag_json_result;
    }

    // 3. 제목 수정 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTitleRequest {
        private String title;
    }

    // 4. 상세 분석 결과 응답 DTO (AnalysisResultResponse)
    @Getter
    @Builder
    public static class AnalysisResultResponse {
        private Integer analysisId;
        private String url; // (analyzedUrl)
        private String ipAddress;
        private String ipLocation;
        private String safeBrowsingResult;
        private String analysisTitle;
        private RiskLevel riskLevel;
        private String reason;
        private String analysisDetailsJson;
        private String createdAt;
        private String updatedAt;

        public static AnalysisResultResponse fromEntity(Analysis analysis) {
            return AnalysisResultResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    .url(analysis.getAnalyzedUrl())
                    .ipAddress(analysis.getIpAddress())
                    .ipLocation(analysis.getIpLocation())
                    .safeBrowsingResult(analysis.getSafeBrowsingResult())
                    .analysisTitle(analysis.getAnalysisTitle())
                    .riskLevel(analysis.getRiskLevel())
                    .reason(analysis.getReason())
                    .analysisDetailsJson(analysis.getAnalysisDetailsJson())
                    .createdAt(analysis.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(analysis.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }


    // =====================================================================
    // ★ 5. (핵심 수정) 히스토리 목록 응답 DTO (AnalysisHistoryResponse)
    // =====================================================================
    @Getter
    @Builder
    public static class AnalysisHistoryResponse {

        private Integer analysisId;
        private String analysisTitle; // ★ 2. (수정) 이 필드가 누락되었습니다.
        private String analysisUrl;   // (참고: 프론트에서 title이 null일 때 사용할 URL)
        private String createdAt;

        public static AnalysisHistoryResponse fromEntity(Analysis analysis) {

            // 날짜만 표시 (YYYY-MM-DD)
            String formattedDate = analysis.getCreatedAt()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);

            return AnalysisHistoryResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    // ★ 3. (수정) analysisTitle 매핑 추가
                    .analysisTitle(analysis.getAnalysisTitle())
                    // (참고: 프론트에서 || 연산자로 사용)
                    .analysisUrl(analysis.getAnalyzedUrl())
                    .createdAt(formattedDate) // (포맷된 날짜 사용)
                    .build();
        }
    }
}