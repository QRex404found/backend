package com.found.qrex.repository;

import com.found.qrex.domain.Board;
import com.found.qrex.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {

    Page<Board> findByUser(User user, Pageable pageable);

    void deleteByUser(User user);

    // 최신순 조회
    Page<Board> findAllByOrderByBoardIdDesc(Pageable pageable);

    // 🔍 제목은 대소문자 무시, 내용은 기본 검색 (CLOB 문제 방지)
    List<Board> findByPostTitleContainingIgnoreCaseOrPostContentsContaining(
            String titleKeyword, String contentKeyword);

    // 🔥 정확한 제목 일치 검색 (대소문자 무시)
    List<Board> findByPostTitleIgnoreCase(String title);

    // 🔹 부분 검색 (대소문자 무시)
    List<Board> findByPostTitleContainingIgnoreCase(String title);
}
