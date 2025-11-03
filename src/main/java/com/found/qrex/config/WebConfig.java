package com.found.qrex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * ✅ [핵심 수정]
     * 이 메서드의 CORS 설정이 SecurityConfig의 전역 설정을 덮어쓰고 있었습니다.
     * SecurityConfig에서 CORS를 중앙 관리하기 위해 이 메서드 전체를 주석 처리합니다.
     */
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // "/api/"로 시작하는 모든 요청
                .allowedOrigins(
                        // (기본) 로컬 테스트용
                        "http://localhost:5173",
                        "http://localhost:3000",

                        // (필수) 지금 에러가 뜬 프론트엔드(윈도우)의 IP와 포트
                        "http://172.30.133.113:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 인증 정보(쿠키, 토큰 헤더) 허용
                .maxAge(3600); // 3600초 (1시간) 동안 pre-flight 캐시
    }
    */
}