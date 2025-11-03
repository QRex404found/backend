// src/main/java/com/found/qrex/service/SafeBrowsingService.java

package com.found.qrex.service;

import com.found.qrex.dto.SafeBrowsingRequest;
import com.found.qrex.dto.SafeBrowsingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List; // <-- 임포트 확인

@Service
public class SafeBrowsingService {

    private final WebClient webClient;
    private final String safeBrowsingApiKey;

    public SafeBrowsingService(
            WebClient.Builder webClientBuilder,
            @Value("${google.safe-browsing.api-key}") String apiKey,
            @Value("${google.safe-browsing.lookup-url}") String lookupUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(lookupUrl).build();
        this.safeBrowsingApiKey = apiKey;
    }

    /**
     * Google Safe Browsing API를 호출하여 특정 URL의 위험도를 확인합니다.
     * [복구] 실제 API 호출 로직을 복구합니다.
     * @param targetUrl 검사할 URL 문자열
     * @return "SAFE" 또는 "RISKY" 또는 API 오류 시 "ERROR_CHECK" 반환
     */
    public String checkUrlSafety(String targetUrl) {

        // 1. 요청 DTO 생성
        SafeBrowsingRequest requestBody = new SafeBrowsingRequest(targetUrl);

        // 🌟 [복구] 실제 API 호출 try-catch 블록을 복구합니다.
        try {
            // 2. WebClient를 사용한 POST 요청
            SafeBrowsingResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", safeBrowsingApiKey).build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SafeBrowsingResponse.class)
                    .block();

            // 3. 결과 파싱
            if (response != null && response.getMatches() != null && !response.getMatches().isEmpty()) {
                return "RISKY";
            }
            return "SAFE";

        } catch (WebClientResponseException e) {
            // 4. API 호출 실패 (HTTP 에러) 처리
            System.err.printf("Safe Browsing API 호출 중 HTTP 오류 발생. 상태 코드: %d\n응답 본문: %s\n",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return "ERROR_CHECK";
        } catch (Exception e) {
            // 5. 기타 예외 (네트워크 문제 등) 처리
            System.err.println("Safe Browsing API 호출 중 기타 예외 발생: " + e.getMessage());
            return "ERROR_CHECK";
        }
    }
}