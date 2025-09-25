// QR 코드 스캔, 피싱 여부 판별, 분석 기록 저장 및 조회 로직을 처리합니다.
package com.found.qrex.service;

import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.User;
import com.found.qrex.dto.AnalysisDto;
import com.found.qrex.repository.AnalysisRepository;
import com.found.qrex.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    public AnalysisService(AnalysisRepository analysisRepository, UserRepository userRepository) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder().baseUrl(pythonServerUrl).build();
    }

    // 현재 로그인된 사용자를 가져오는 도우미 메서드
    private User getCurrentUser() {
        return userRepository.findByUserId("dummyUser").orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public AnalysisDto.AnalysisResultResponse scanAndAnalyze(MultipartFile qrImage) {
        try {
            // 1. Python 서버로 이미지 전송 및 분석 요청
            AnalysisDto.AnalysisResultResponse pythonResponse = webClient.post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("image", qrImage.getResource()))
                    .retrieve()
                    .bodyToMono(AnalysisDto.AnalysisResultResponse.class)
                    .block();

            // 2. 분석 결과 DB에 저장
            Analysis analysis = new Analysis();
            analysis.setUser(getCurrentUser());
            analysis.setScanResult(pythonResponse.getStatus());
            analysis.setAnalysisUrl(pythonResponse.getUrl());
            analysis.setIpAddress(pythonResponse.getIpAddress());
            analysis.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            analysisRepository.save(analysis);

            return pythonResponse;

        } catch (Exception e) { // WebClient 호출에서 발생하는 일반적인 예외 처리
            throw new RuntimeException("QR 이미지 분석 중 오류 발생", e);
        }
    }

    public Page<AnalysisDto.AnalysisHistoryResponse> getAnalysisHistory(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Analysis> analyses = analysisRepository.findByUser(currentUser, pageable);
        return analyses.map(analysis -> {
            AnalysisDto.AnalysisHistoryResponse dto = new AnalysisDto.AnalysisHistoryResponse();
            dto.setAnalysisId(analysis.getAnalysisId());
            dto.setAnalysisUrl(analysis.getAnalysisUrl());
            dto.setScanResult(analysis.getScanResult());
            dto.setCreatedAt(analysis.getCreatedAt());
            return dto;
        });
    }

    public AnalysisDto.AnalysisResultResponse getAnalysisResult(Integer analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));
        AnalysisDto.AnalysisResultResponse response = new AnalysisDto.AnalysisResultResponse();
        response.setUrl(analysis.getAnalysisUrl());
        response.setStatus(analysis.getScanResult());
        response.setTitle(analysis.getAnalysisTitle());
        response.setIpAddress(analysis.getIpAddress());
        return response;
    }

    @Transactional
    public void updateAnalysisTitle(Integer analysisId, String title) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 기록을 찾을 수 없습니다."));
        analysis.setAnalysisTitle(title);
        analysis.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        analysisRepository.save(analysis);
    }
}