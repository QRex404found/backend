package com.found.qrex.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    private final String aiServerUrl;

    // application.properties에 있는 AI 서버 주소(8081)를 가져옵니다.
    public AiProxyController(@Value("${python.server.rag-url}") String aiServerUrl) {
        this.aiServerUrl = aiServerUrl;
    }

    @GetMapping("/chat")
    public ResponseEntity<String> proxyChat(
            @RequestParam("message") String message,
            // ⭐️ [추가] 프론트엔드에서 비로그인 사용자를 식별하기 위해 보낸 임시 ID를 받습니다.
            @RequestParam(value = "userId", required = false) String guestId
    ) {
        // 1. 로그인 여부 및 실제 사용자 ID 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        // 2. 실제 AI에게 넘겨줄 ID 결정
        // 로그인했다면? -> 인증 정보에서 진짜 ID(auth.getName()) 사용
        // 로그인 안 했다면? -> 프론트에서 준 guestId 사용 (없으면 "guest")
        String realUserId = isLoggedIn ? auth.getName() : (guestId != null ? guestId : "guest");

        RestClient restClient = RestClient.create();

        try {
            // ⭐️ [핵심 수정] URL에 conversationId와 userId 파라미터를 추가했습니다.
            // conversationId와 userId 모두 realUserId로 설정하여 대화 기억과 도구 실행을 연결합니다.
            String aiResponse = restClient.get()
                    .uri(aiServerUrl + "/api/agent/chat?message={msg}&isLoggedIn={login}&conversationId={cid}&userId={uid}",
                            message, isLoggedIn, realUserId, realUserId)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(aiResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI 서버 연결 실패: " + e.getMessage());
        }
    }
}