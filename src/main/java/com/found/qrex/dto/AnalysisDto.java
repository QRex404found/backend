//QR 분석 요청 및 결과를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class AnalysisDto {
    @Getter
    @Setter
    public static class AnalysisResultResponse {
        private String status;
        private String url;
        private String title;
        private String ipAddress;
    }

    @Getter
    @Setter
    public static class AnalysisHistoryResponse {
        private Integer analysisId;
        private String analysisUrl;
        private String scanResult;
        private Timestamp createdAt;
    }

    @Getter
    @Setter
    public static class UpdateTitleRequest {
        private String title;
    }
}