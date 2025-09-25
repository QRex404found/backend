// CommentRepository.java
package com.found.qrex.repository;

import com.found.qrex.domain.Comment;
import com.found.qrex.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByBoard(Board board);
}