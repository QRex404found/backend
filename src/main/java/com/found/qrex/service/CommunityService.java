// 게시글 및 댓글의 생성, 조회, 수정, 삭제 로직을 처리합니다.
package com.found.qrex.service;

import com.found.qrex.domain.Board;
import com.found.qrex.domain.Comment;
import com.found.qrex.domain.User;
import com.found.qrex.dto.BoardDto;
import com.found.qrex.dto.CommentDto;
import com.found.qrex.repository.BoardRepository;
import com.found.qrex.repository.CommentRepository;
import com.found.qrex.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommunityService(BoardRepository boardRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // 현재 로그인된 사용자를 가져오는 도우미 메서드
    private User getCurrentUser() {
        // 실제 구현에서는 Spring Security Context에서 사용자 ID를 가져와야 합니다.
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 게시글 작성
    @Transactional
    public void createPost(BoardDto.BoardCreateRequest request) {
        if (request.getPostTitle() == null || request.getPostTitle().isEmpty() ||
                request.getPostContents() == null || request.getPostContents().isEmpty()) {
            throw new IllegalArgumentException("제목과 내용을 모두 입력해야 합니다.");
        }
        Board board = new Board();
        board.setUser(getCurrentUser());
        board.setPostTitle(request.getPostTitle());
        board.setUrl(request.getUrl());
        board.setPostContents(request.getPostContents());
        board.setImagePath(request.getImagePath());
        board.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        boardRepository.save(board);
    }

    // 게시글 목록 조회
    public Page<BoardDto.BoardResponse> getPosts(Pageable pageable) {
        Page<Board> posts = boardRepository.findAll(pageable);
        return posts.map(post -> {
            BoardDto.BoardResponse dto = new BoardDto.BoardResponse();
            dto.setBoardId(post.getBoardId());
            dto.setTitle(post.getPostTitle());
            dto.setCreatedAt(post.getCreatedAt());
            return dto;
        });
    }

    // 게시글 상세 조회
    public BoardDto.BoardDetailResponse getPostDetail(Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        BoardDto.BoardDetailResponse response = new BoardDto.BoardDetailResponse();
        response.setBoardId(board.getBoardId());
        response.setTitle(board.getPostTitle());
        response.setUserId(board.getUser().getUserId());
        response.setImagePath(board.getImagePath());
        response.setUrl(board.getUrl());
        response.setContents(board.getPostContents());
        response.setCreatedAt(board.getCreatedAt());

        // 댓글 목록 조회
        List<CommentDto.CommentResponse> comments = commentRepository.findByBoard(board).stream()
                .map(comment -> {
                    CommentDto.CommentResponse commentDto = new CommentDto.CommentResponse();
                    commentDto.setCommentId(comment.getCommentId());
                    commentDto.setUserId(comment.getUser().getUserId());
                    commentDto.setContents(comment.getCommentContents());
                    commentDto.setCreatedAt(comment.getCreatedAt());
                    return commentDto;
                })
                .collect(Collectors.toList());
        response.setComments(comments);
        return response;
    }

    // 댓글 작성
    @Transactional
    public void addComment(Integer boardId, CommentDto.CommentRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setUser(getCurrentUser());
        comment.setCommentContents(request.getContents());
        comment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        commentRepository.save(comment);
    }

    // 사용자가 작성한 게시글 조회
    public Page<BoardDto.BoardResponse> getMyPosts(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Board> myPosts = boardRepository.findByUser(currentUser, pageable);
        return myPosts.map(post -> {
            BoardDto.BoardResponse dto = new BoardDto.BoardResponse();
            dto.setBoardId(post.getBoardId());
            dto.setTitle(post.getPostTitle());
            dto.setCreatedAt(post.getCreatedAt());
            return dto;
        });
    }
}