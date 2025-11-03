//DB: ANALYSIS
package com.found.qrex.domain;
// lombok.Builder를 사용하기 위해 lombok.*; 대신 필요한 것들을 명시적으로 임포트합니다.

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Builder 어노테이션 추가
import java.time.LocalDateTime; // Timestamp 대신 최신 Java API를 사용하도록 변경

@Entity
@Table(name = "ANALYSIS") // 기존 테이블 이름 유지
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    // 1. 기존 DB 필드 및 RAG 입력/출력 통합
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ANALYSIS_ID", nullable = false) // 기존 ANALYSIS_ID 사용
    private Integer analysisId; // ID 타입은 Integer로 유지

    // RAG 입력 데이터 (추가 필드)
    @Column(name = "ANALYZED_URL", length = 2083) // URL 필드는 분석 결과를 위해 유지
    private String analyzedUrl;          // 사용자가 스캔한 원본 URL (analysisUrl 대신 사용 권장)

    @Column(name = "IP_ADDRESS", length = 45) // 기존 IP_ADDRESS 필드 유지
    private String ipAddress;

    @Column(name = "IP_LOCATION", length = 255) // GeoIP로 획득한 IP 위치 (추가)
    private String ipLocation;

    @Column(name = "SAFE_BROWSING_RESULT", length = 50) // Google API 결과 (추가)
    private String safeBrowsingResult;

    @Column(name = "ANALYSIS_TITLE", length = 255) // 기존 ANALYSIS_TITLE 필드 유지
    private String analysisTitle;        // 사용자가 직접 입력하는 제목

    // RAG 출력 데이터 (SCAN_RESULT 대체 및 상세 필드 추가)
    @Enumerated(EnumType.STRING)
    @Column(name = "RISK_LEVEL", length = 50) // SCAN_RESULT 대신 RISK_LEVEL로 명시적으로 변경
    private RiskLevel riskLevel;        // 최종 위험도 (안전, 의심, 위험)

    @Column(name = "REASON", columnDefinition = "TEXT") // LLM이 판단한 근거 (추가)
    private String reason;

    @Column(name = "ANALYSIS_DETAILS_JSON", columnDefinition = "TEXT") // FastAPI JSON 원본 (추가)
    private String analysisDetailsJson;

    // 2. 타임스탬프 필드 수정 및 통합
    // JPA의 최신 DateTime API를 사용하도록 Timestamp 대신 LocalDateTime 사용
    // DB에서 Timestamp 타입과 호환되도록 설정 (DB DDL 생성 시)
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // 3. 기존 USER 관계 유지
    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private User user;

    // [참고] 기존의 SCAN_RESULT와 ANALYSIS_URL 필드는 RAG 필드와 통합되거나 이름이 변경되었습니다.
    // - SCAN_RESULT -> RISK_LEVEL (Enum)으로 변경
    // - ANALYSIS_URL -> ANALYZED_URL (RAG 입력 URL)로 변경
}