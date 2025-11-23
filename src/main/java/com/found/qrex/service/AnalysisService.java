package com.found.qrex.service;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.found.qrex.domain.Analysis;

import com.found.qrex.domain.User;

import com.found.qrex.domain.RiskLevel;

import com.found.qrex.dto.AnalysisDto;

import com.found.qrex.repository.AnalysisRepository;

import com.found.qrex.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.springframework.web.util.UriComponentsBuilder;



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



    public AnalysisService(

            AnalysisRepository analysisRepository,

            UserRepository userRepository,

            GeoLocationService geoLocationService,

            SafeBrowsingService safeBrowsingService,

            ObjectMapper objectMapper,

            WebClient.Builder webClientBuilder,

            @Value("${python.server.rag-url}") String ragServerUrl

    ) {

        this.analysisRepository = analysisRepository;

        this.userRepository = userRepository;

        this.geoLocationService = geoLocationService;

        this.safeBrowsingService = safeBrowsingService;

        this.objectMapper = objectMapper;

        this.webClient = webClientBuilder.baseUrl(ragServerUrl).build();

    }



    private User getCurrentUser() {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUserId(userId)

                .orElseThrow(() -> new RuntimeException("User not found (ID: " + userId + ")"));

    }



    @Transactional

    public Analysis scanAndAnalyze(String url) throws Exception {



        log.info(">>>> scanAndAnalyze 메소드 실행됨 (URL: {})", url);



        String ipLocation = geoLocationService.getIpLocation(url);

        String ipAddress = geoLocationService.getIpAddress(url);

        String safeBrowsing = safeBrowsingService.checkUrlSafety(url);



        log.info(">>>> RAG 서버로 요청 전송 중... (IP: {}, SafeBrowsing: {})", ipLocation, safeBrowsing);



        String jsonResponseString;

        try {

            jsonResponseString = webClient.get()

                    .uri(uriBuilder -> uriBuilder

                            .path("/api/rag")

                            .queryParam("question", url)

                            .queryParam("ip_location", ipLocation)

                            .queryParam("safe_browsing_result", safeBrowsing)

                            .build())

                    .retrieve()

                    .bodyToMono(String.class)

                    .block();



        } catch (WebClientResponseException e) {

            log.error("RAG 서버 응답 오류: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("RAG 분석 서버 통신 실패", e);

        }



        if (jsonResponseString == null || jsonResponseString.isEmpty()) {

            throw new IllegalStateException("RAG 분석 서버로부터 빈 응답을 받았습니다.");

        }



        Map<String, Object> ragResult = objectMapper.readValue(jsonResponseString, Map.class);



        if (ragResult.containsKey("error")) {

            String errorMsg = (String) ragResult.get("error");

            log.error("RAG Server Runtime Error: {}", errorMsg);

            throw new RuntimeException("RAG 서버 내부 API 통신 실패 (Gemini): " + errorMsg);

        }



        String riskLevelStr = (String) ragResult.get("risk_level");

        String reason = (String) ragResult.get("reason");



        log.info(">>>> 분석 완료. 위험도: {}", riskLevelStr);



        Analysis analysis = Analysis.builder()

                .user(getCurrentUser())

                .analyzedUrl(url)

                .ipAddress(ipAddress)

                .ipLocation(ipLocation)

                .safeBrowsingResult(safeBrowsing)

                .riskLevel(RiskLevel.valueOf(riskLevelStr.trim().replace(" ", "_").toUpperCase()))

                .reason(reason)

                .analysisDetailsJson(jsonResponseString)

                .analysisTitle(url)

                .createdAt(LocalDateTime.now())

                .updatedAt(LocalDateTime.now())

                .build();



        return analysisRepository.save(analysis);

    }



    @Transactional(readOnly = true)

    public Page<AnalysisDto.AnalysisHistoryResponse> getAnalysisHistory(Pageable pageable) {

        User currentUser = getCurrentUser();

        Page<Analysis> analyses = analysisRepository.findByUser(currentUser, pageable);

        return analyses.map(AnalysisDto.AnalysisHistoryResponse::fromEntity);

    }



    @Transactional(readOnly = true)

    public AnalysisDto.AnalysisResultResponse getAnalysisResult(Integer analysisId) {

        Analysis analysis = analysisRepository.findById(analysisId)

                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));



        User currentUser = getCurrentUser();

        if (!analysis.getUser().getUserId().equals(currentUser.getUserId())) {

            throw new IllegalStateException("이 기록을 조회할 권한이 없습니다.");

        }



        return AnalysisDto.AnalysisResultResponse.fromEntity(analysis);

    }



    @Transactional

    public void updateAnalysisTitle(Integer analysisId, String title) {

        User currentUser = getCurrentUser();

        Analysis analysis = analysisRepository.findById(analysisId)

                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));



        if (!analysis.getUser().getUserId().equals(currentUser.getUserId())) {

            throw new IllegalStateException("이 기록을 수정할 권한이 없습니다.");

        }



        analysis.setAnalysisTitle(title);

        analysis.setUpdatedAt(LocalDateTime.now());

        analysisRepository.save(analysis);

    }



    @Transactional(readOnly = true)

    public Page<AnalysisDto.AnalysisHistoryResponse> getHistoryByUserId(String userId, Pageable pageable) {



        Pageable fixedPageable = PageRequest.of(

                pageable.getPageNumber(),

                pageable.getPageSize(),

                Sort.by(Sort.Direction.DESC, "CREATED_AT")

        );



        Page<Analysis> result = analysisRepository.findByUserIdNative(userId, fixedPageable);



        return result.map(AnalysisDto.AnalysisHistoryResponse::fromEntity);

    }



// ==================================================

// 🔥 updateTitleByAi() 개선 - 최신 기록 자동 선택

// ==================================================

    @Transactional

    public Analysis updateTitleByAi(Integer analysisId, String analyzedUrl, String userId, String newTitle) {



        User user = userRepository.findByUserId(userId)

                .orElse(null);



        Analysis target = null;



// 1️⃣ analysisId가 전달되면 우선 적용

        if (analysisId != null) {

            target = analysisRepository.findById(analysisId).orElse(null);

        }



// 2️⃣ analysisId가 없고 URL이 전달되면 → 최신 URL 분석 기록 선택

        if (target == null && analyzedUrl != null && user != null) {

            target = analysisRepository.findFirstByAnalyzedUrlAndUserOrderByCreatedAtDesc(analyzedUrl, user);

        }



// 3️⃣ 그래도 없으면 → 사용자 최신 분석 기록 사용

        if (target == null && user != null) {

            target = analysisRepository.findFirstByUserOrderByCreatedAtDesc(user);

        }



        if (target == null) {

            throw new IllegalArgumentException("수정할 분석 결과를 찾을 수 없습니다.");

        }



        target.setAnalysisTitle(newTitle);

        target.setUpdatedAt(LocalDateTime.now());



        return analysisRepository.saveAndFlush(target);

    }



// ==================================================

// 🔥 Repository를 위한 Helper 메서드 추가

// ==================================================

    @Transactional(readOnly = true)

    public Analysis getAnalysisEntity(Integer analysisId) {

        return analysisRepository.findById(analysisId).orElse(null);

    }

}