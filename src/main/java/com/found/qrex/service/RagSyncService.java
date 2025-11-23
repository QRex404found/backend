package com.found.qrex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Map;

@Service
public class RagSyncService {

    private static final Logger log = LoggerFactory.getLogger(RagSyncService.class);

    // AI 서버 주소 (로컬)
    private final RestClient restClient = RestClient.create("http://localhost:8081");

    // @Async: 이 메서드는 백그라운드에서 실행됩니다. (사용자는 기다리지 않음)
    @Async
    public void syncToRag(String sourceType, String id, String content, String userId, String createdAt) {
        try {
            log.info(">>>> [RAG Sync] 데이터 전송 시작: Type={}, ID={}", sourceType, id);

            Map<String, String> body = Map.of(
                    "sourceType", sourceType,
                    "id", id,
                    "content", content, // 검색될 실제 텍스트 내용
                    "userId", userId != null ? userId : "anonymous",
                    "createdAt", createdAt != null ? createdAt : ""
            );

            restClient.post()
                    .uri("/api/rag/ingest") // AI 서버의 수집 엔드포인트
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info(">>>> [RAG Sync] 전송 완료");

        } catch (Exception e) {
            // AI 서버가 꺼져있어도 메인 서버는 죽으면 안 됨 -> 에러 로그만 남김
            log.error(">>>> [RAG Sync] 전송 실패 (AI 서버 확인 필요): {}", e.getMessage());
        }
    }
}