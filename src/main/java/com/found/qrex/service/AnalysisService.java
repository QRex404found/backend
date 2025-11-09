package com.found.qrex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.User;
import com.found.qrex.domain.RiskLevel;
import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.repository.AnalysisRepository;
import com.found.qrex.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional; // ★ 1. 트랜잭션 임포트
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    private final GeoLocationService geoLocationService;
    private final SafeBrowsingService safeBrowsingService;
    private final ObjectMapper objectMapper;

    // =========================================================================
    // 생성자 (기존과 동일)
    // =========================================================================
    public AnalysisService(
            AnalysisRepository analysisRepository,
            UserRepository userRepository,
            GeoLocationService geoLocationService,
            SafeBrowsingService safeBrowsingService,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            @Value("${python.server.rag-url}") String pythonServerRagUrl
    ) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.geoLocationService = geoLocationService;
        this.safeBrowsingService = safeBrowsingService;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(pythonServerRagUrl).build();
    }
    // =========================================================================

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found (ID: " + userId + ")"));
    }

    // =========================================================================
    // 1. 핵심 RAG 분석 로직 (기존과 동일)
    // =========================================================================
    @Transactional
    public Analysis scanAndAnalyze(String url) throws Exception {

        log.info(">>>> scanAndAnalyze 메소드 실행됨 (URL: {})", url);

        // 1. 외부 정보 획득 (전처리)
        String ipLocation = geoLocationService.getIpLocation(url);
        String ipAddress = geoLocationService.getIpAddress(url);
        String safeBrowsing = safeBrowsingService.checkUrlSafety(url);

        // 2. FastAPI RAG 모듈로 요청 전송을 위한 DTO 생성
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
                .analysisTitle(url) // (중요) 기본 제목을 URL로 설정
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info(">>>> 신규 분석 기록 저장 완료 (소유자: {})", analysis.getUser().getUserId());
        return analysisRepository.save(analysis);
    }

    // =========================================================================
    // 2. (★수정★) DTO 변환 로직 (읽기 전용 트랜잭션 추가)
    // =========================================================================

    // ★ 2. [수정] (readOnly = true) 추가
    // 이 어노테이션이 없으면 '페이지네이션' 시 최신 제목을 못 가져옵니다.
    @Transactional(readOnly = true)
    public Page<AnalysisDto.AnalysisHistoryResponse> getAnalysisHistory(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Analysis> analyses = analysisRepository.findByUser(currentUser, pageable);
        return analyses.map(AnalysisDto.AnalysisHistoryResponse::fromEntity);
    }

    // ★ 3. [수정] (readOnly = true) 추가
    // 이 어노테이션이 없으면 '새로고침' 시 최신 제목을 못 가져옵니다.
    @Transactional(readOnly = true)
    public AnalysisDto.AnalysisResultResponse getAnalysisResult(Integer analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));

        User currentUser = getCurrentUser();
        if (!analysis.getUser().getUserId().equals(currentUser.getUserId())) {
            log.warn(">>>> getAnalysisResult 권한 검사 실패! (소유자 불일치) {} != {}",
                    analysis.getUser().getUserId(), currentUser.getUserId());
            throw new IllegalStateException("이 기록을 조회할 권한이 없습니다.");
        }

        return AnalysisDto.AnalysisResultResponse.fromEntity(analysis);
    }
    // =========================================================================


    // =========================================================================
    // 3. (★수정★) 제목 업데이트 로직 (권한 검사 수정)
    // =========================================================================
    @Transactional
    public void updateAnalysisTitle(Integer analysisId, String title) {

        log.info(">>>> updateAnalysisTitle 메소드 실행됨 (ID: {})", analysisId);
        User currentUser = getCurrentUser();
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));

        log.info(">>>> 현재 사용자 ID: {}", currentUser.getUserId());
        log.info(">>>> 기록 소유자 ID: {}", analysis.getUser().getUserId());

        // (중요) 사용자 객체(User) 비교가 아닌, ID(String)를 비교해야 합니다.
        if (!analysis.getUser().getUserId().equals(currentUser.getUserId())) {

            log.warn(">>>> updateAnalysisTitle 권한 검사 실패! (소유자 불일치) {} != {}",
                    analysis.getUser().getUserId(), currentUser.getUserId());

            throw new IllegalStateException("이 기록을 수정할 권한이 없습니다. (소유자 불일치)");
        }

        log.info(">>>> 권한 검사 통과. 제목 수정을 진행합니다.");

        // (정상) 소유자가 일치하면 제목을 수정하고 저장
        analysis.setAnalysisTitle(title);
        analysis.setUpdatedAt(LocalDateTime.now());
        analysisRepository.save(analysis);
    }
}