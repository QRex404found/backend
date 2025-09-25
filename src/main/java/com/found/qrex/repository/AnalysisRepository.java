package com.found.qrex.repository;

import com.found.qrex.domain.Analysis;
import com.found.qrex.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Integer> {
    Page<Analysis> findByUser(User user, Pageable pageable);
}