//API 응답 결과를 담을 구조 정의
package com.found.qrex.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Google Safe Browsing API의 threatMatches:find 엔드포인트로부터 받는 응답 본문 구조를 정의합니다.
 */
@Getter
@Setter
@NoArgsConstructor // Lombok을 사용하여 기본 생성자 생성
public class SafeBrowsingResponse {

    /**
     * 위협 감지 결과 목록입니다.
     * URL이 안전하다면 이 리스트는 null이거나 비어있습니다.
     */
    private List<ThreatMatch> matches;

    // --- 내부 클래스 정의 ---

    /**
     * 감지된 하나의 위협에 대한 상세 정보를 담는 클래스입니다.
     */
    @Getter
    @Setter
    public static class ThreatMatch {
        private String threatType;       // 예: MALWARE, PHISHING
        private String platformType;     // 예: ANY_PLATFORM
        private String threatEntryType;  // 예: URL

        private ThreatEntry threat;      // 감지된 위협 항목 (URL)
        private String cacheDuration;    // 이 결과를 캐시할 수 있는 기간

        // 메타데이터 필드는 선택적이며, 추가적인 정보를 포함할 수 있습니다.
        private ThreatEntryMetadata threatEntryMetadata;
    }

    /**
     * 감지된 위협 항목 (대부분 요청했던 URL이 다시 들어옵니다)
     */
    @Getter
    @Setter
    public static class ThreatEntry {
        private String url;
    }

    /**
     * 위협에 대한 추가 정보를 포함할 수 있는 메타데이터 클래스 (선택적)
     */
    @Getter
    @Setter
    public static class ThreatEntryMetadata {
        // 메타데이터는 key/value 쌍으로 이루어지지만, 여기서는 String으로 처리합니다.
        // 필요에 따라 Map<String, String> 등으로 변경할 수 있습니다.
        // 현재는 API 응답의 일반적인 형태를 따르기 위해 간단하게 정의합니다.
    }
}
