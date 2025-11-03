//API 요청에 필요한 URL 및 클라이언트 정보 정의
package com.found.qrex.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Google Safe Browsing API의 threatMatches:find 엔드포인트에 전송할 요청 본문 구조를 정의합니다.
 */
@Getter
@Setter
@NoArgsConstructor // Lombok을 사용하여 기본 생성자 생성
public class SafeBrowsingRequest {

    private Client client;
    private ThreatInfo threatInfo;

    // --- 내부 클래스 정의 ---

    /**
     * 클라이언트(당신의 애플리케이션) 정보를 담는 클래스입니다.
     */
    @Getter
    @Setter
    // 생성자 주입을 통해 클라이언트 정보를 초기화합니다.
    @AllArgsConstructor
    public static class Client {
        // 프로젝트 식별자 (API 사용 통계를 위해 필요)
        private String clientId = "qrex-url-detector-backend";
        private String clientVersion = "1.0.0";
    }

    /**
     * 검사할 위협 유형 및 대상(URL) 정보를 담는 클래스입니다.
     */
    @Getter
    @Setter
    public static class ThreatInfo {
        // ⭐️ 검사할 위협 유형 목록. 주요 유형을 포함시켰습니다.
        private List<String> threatTypes = List.of(
                "MALWARE",
                "SOCIAL_ENGINEERING",
                "UNWANTED_SOFTWARE",
                "POTENTIALLY_HARMFUL_APPLICATION"
        );

        // 검사할 플랫폼 유형 (대부분 ANY_PLATFORM을 사용)
        private List<String> platformTypes = List.of("ANY_PLATFORM");

        // 검사할 항목 유형 (URL 검사이므로 URL 사용)
        private List<String> threatEntryTypes = List.of("URL");

        // ⭐️ 실제 검사 대상 URL 객체 목록이 담기는 곳
        private List<ThreatEntry> threatEntries;
    }

    /**
     * 실제 검사 대상 URL을 담는 클래스입니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreatEntry {
        private String url;
    }

    // 편의를 위한 생성자: 서비스 계층에서 요청 객체를 쉽게 만들 수 있게 합니다.
    public SafeBrowsingRequest(String url) {
        this.client = new Client("qrex-url-detector-backend", "1.0.0");

        ThreatEntry entry = new ThreatEntry(url);

        ThreatInfo info = new ThreatInfo();
        info.setThreatEntries(List.of(entry));

        this.threatInfo = info;
    }
}
