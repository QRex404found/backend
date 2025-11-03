package com.found.qrex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.User;
import com.found.qrex.domain.RiskLevel;
import com.found.qrex.dto.AnalysisDto; // 🌟 1. AnalysisDto만 임포트
import com.found.qrex.repository.AnalysisRepository;
import com.found.qrex.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    // RAG 통합에 필요한 서비스 추가
    private final GeoLocationService geoLocationService;
    private final SafeBrowsingService safeBrowsingService;
    private final ObjectMapper objectMapper;

// 🚨 @Value 필드 주입을 삭제합니다. (생성자에서 주입받아야 함)
// @Value("${python.server.rag-url}")
// private String pythonServerRagUrl;

    // =========================================================================
// 🌟 수정 1: FastAPI 연결 오류(Connection refused :80) 해결
// 생성자 주입 수정: @Value를 필드가 아닌 생성자 파라미터로 주입받습니다.
// WebClient.Builder도 주입받습니다.
// =========================================================================
    public AnalysisService(
            AnalysisRepository analysisRepository,
            UserRepository userRepository,
            GeoLocationService geoLocationService,
            SafeBrowsingService safeBrowsingService,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder, // 🌟 WebClient.Builder 주입
            @Value("${python.server.rag-url}") String pythonServerRagUrl // 🌟 @Value로 URL 직접 주입
    ) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.geoLocationService = geoLocationService;
        this.safeBrowsingService = safeBrowsingService;
        this.objectMapper = objectMapper;

// 🌟 주입받은 URL로 WebClient 빌드 (이제 pythonServerRagUrl이 null이 아님)
        this.webClient = webClientBuilder.baseUrl(pythonServerRagUrl).build();
    }
// =========================================================================

    private User getCurrentUser() {
// SecurityContext에서 현재 인증된 사용자의 ID (JWT 토큰에서 추출된 ID)를 가져옵니다.
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found (ID: " + userId + ")"));
    }

    // =========================================================================
// 1. 핵심 RAG 분석 로직 (URL을 입력받아 FastAPI로 전송)
// =========================================================================
    @Transactional
    public Analysis scanAndAnalyze(String url) throws Exception {
// 1. 외부 정보 획득 (전처리)
// 🌟 수정 2: GeoIP/DNS 및 Safe Browsing 오류 우회
// (GeoLocationService.java와 SafeBrowsingService.java의 내부 로직을 수정하여
// 실제 API 호출 대신 더미 데이터를 반환하도록 수정해야 합니다.)
        String ipLocation = geoLocationService.getIpLocation(url); // 예: "Germany" (더미) 반환
        String ipAddress = geoLocationService.getIpAddress(url); // 예: "192.168.1.1" (더미) 반환
        String safeBrowsing = safeBrowsingService.checkUrlSafety(url); // 예: "Unverified" (더미) 반환

// 2. FastAPI RAG 모듈로 요청 전송을 위한 DTO 생성 (AnalysisDto 내부 클래스 사용)
        AnalysisDto.RAGAnalysisRequest ragRequest = new AnalysisDto.RAGAnalysisRequest(url, ipLocation, safeBrowsing);

        AnalysisDto.RAGAnalysisResponse ragResponse;
        try {
            ragResponse = webClient.post()
                    .uri("/analyze-qr") // FastAPI 엔드포인트
                    .bodyValue(ragRequest)
                    .retrieve()
                    .bodyToMono(AnalysisDto.RAGAnalysisResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("FastAPI RAG 서버 응답 오류: " + e.getRawStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("RAG 분석 서버 통신 실패", e);
        }

// 3. FastAPI 응답 파싱 및 최종 처리
        if (ragResponse == null || ragResponse.getRag_json_result() == null) {
            throw new IllegalStateException("RAG 분석 서버로부터 유효한 응답을 받지 못했습니다.");
        }

// LLM이 반환한 JSON 문자열 파싱
        Map<String, Object> ragResult = objectMapper.readValue(ragResponse.getRag_json_result(), Map.class);
        String riskLevelStr = (String) ragResult.get("risk_level");
        String reason = (String) ragResult.get("reason");

// 4. DB에 저장 (Analysis 엔티티 사용)
        Analysis analysis = Analysis.builder()
                .user(getCurrentUser())
                .analyzedUrl(url)
                .ipAddress(ipAddress)
                .ipLocation(ipLocation)
                .safeBrowsingResult(safeBrowsing)
                .riskLevel(RiskLevel.valueOf(riskLevelStr.trim().replace(" ", "_").toUpperCase()))
                .reason(reason)
                .analysisDetailsJson(ragResponse.getRag_json_result())
                .analysisTitle(url)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return analysisRepository.save(analysis);
    }

// =========================================================================
// 🌟 수정 3: 컨트롤러와의 타입 불일치 해결
// =========================================================================

    public Page<AnalysisDto.AnalysisHistoryResponse> getAnalysisHistory(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Analysis> analyses = analysisRepository.findByUser(currentUser, pageable);

// 🌟 Controller가 DTO Page를 기대하므로, Service에서 DTO로 변환하여 반환합니다.
        return analyses.map(AnalysisDto.AnalysisHistoryResponse::fromEntity);
    }

    public AnalysisDto.AnalysisResultResponse getAnalysisResult(Integer analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));

// 🌟 Controller가 DTO를 기대하므로, Service에서 DTO로 변환하여 반환합니다.
        return AnalysisDto.AnalysisResultResponse.fromEntity(analysis);
    }
// =========================================================================


    // 제목 업데이트 로직은 그대로 유지
    @Transactional
    public void updateAnalysisTitle(Integer analysisId, String title) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));
        analysis.setAnalysisTitle(title);
        analysis.setUpdatedAt(LocalDateTime.now());
        analysisRepository.save(analysis);
    }
}