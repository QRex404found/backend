package com.found.qrex.repository;

import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Integer> {

    // [기존 유지]
    Page<Analysis> findByUser(User user, Pageable pageable);
    void deleteByUser(User user);

    // [기존 유지 - Native Query]
    @Query(value = "SELECT * FROM ANALYSIS WHERE USER_ID = :writerId",
            countQuery = "SELECT count(*) FROM ANALYSIS WHERE USER_ID = :writerId",
            nativeQuery = true)
    Page<Analysis> findByUserIdNative(@Param("writerId") String writerId, Pageable pageable);

    // ======================================================
    // 🔥 [신규 추가] 가장 최근 분석 데이터 검색 메서드
    // ======================================================

    // 특정 URL + 특정 사용자 → 최신 분석 기록 1개
    Analysis findFirstByAnalyzedUrlAndUserOrderByCreatedAtDesc(String analyzedUrl, User user);

    // 특정 사용자 → 최신 분석 기록 1개
    Analysis findFirstByUserOrderByCreatedAtDesc(User user);
}
