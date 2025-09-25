package com.found.qrex.repository;

import com.found.qrex.domain.Board;
import com.found.qrex.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    Page<Board> findByUser(User user, Pageable pageable);
}