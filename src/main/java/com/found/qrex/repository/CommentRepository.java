// CommentRepository.java
package com.found.qrex.repository;

import com.found.qrex.domain.Comment;
import com.found.qrex.domain.Board;
import com.found.qrex.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByBoard(Board board);

    // 유저로 댓글 전체 삭제 기능
    void deleteByUser(User user);
}
