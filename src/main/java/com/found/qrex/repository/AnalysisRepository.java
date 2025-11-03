package com.found.qrex.repository;

import com.found.qrex.domain.Analysis; // 최종 엔티티 클래스
import com.found.qrex.domain.User; // 사용자 엔티티
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 1. 엔티티는 Analysis를 사용합니다.
// 2. ID 타입은 기존 코드에 따라 Integer를 유지합니다.
public interface AnalysisRepository extends JpaRepository<Analysis, Integer> {

    // 기존 기능 보존: 사용자별 분석 기록을 페이지네이션하여 조회하는 기능
    Page<Analysis> findByUser(User user, Pageable pageable);

    // 추가로 필요한 경우:
    // List<Analysis> findByRiskLevel(RiskLevel riskLevel);
}