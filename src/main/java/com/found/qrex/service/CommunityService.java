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

import org.springframework.web.multipart.MultipartFile;



// import java.sql.Timestamp; // 🚨 Timestamp 임포트 삭제 완료!

import java.time.LocalDateTime; // LocalDateTime 사용



import java.util.List;

import java.util.stream.Collectors;



@Service

public class CommunityService {



    private final BoardRepository boardRepository;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;



    private static final int REPORT_LIMIT = 50;



    public CommunityService(BoardRepository boardRepository, CommentRepository commentRepository, UserRepository userRepository, FileStorageService fileStorageService) {

        this.boardRepository = boardRepository;

        this.commentRepository = commentRepository;

        this.userRepository = userRepository;

        this.fileStorageService = fileStorageService;

    }



    private User getCurrentUser() {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

    }



// 게시글 작성

    @Transactional

    public void createPost(BoardDto.BoardCreateRequest request, MultipartFile photoFile) {

        if (request.getPostTitle() == null || request.getPostTitle().isEmpty() ||

                request.getPostContents() == null || request.getPostContents().isEmpty()) {

            throw new IllegalArgumentException("제목과 내용을 모두 입력해야 합니다.");

        }



// 4-1. (추가) 파일 업로드 서비스를 호출하여 URL을 받아옵니다.

        String imageUrl = fileStorageService.storeFile(photoFile);



        Board board = new Board();

        board.setUser(getCurrentUser());

        board.setPostTitle(request.getPostTitle());

        board.setUrl(request.getUrl());

        board.setPostContents(request.getPostContents());



// 4-2. (수정) DTO의 imagePath 대신, 서비스가 반환한 imageUrl을 저장합니다.

        board.setImagePath(imageUrl); // 👈 (null 또는 https://placehold.co/... URL이 저장됨)



        board.setCreatedAt(LocalDateTime.now());

        boardRepository.save(board);

    }



// 게시글 목록 조회

    public Page<BoardDto.BoardResponse> getPosts(Pageable pageable) {

        Page<Board> posts = boardRepository.findAllByOrderByBoardIdDesc(pageable);

        return posts.map(post -> {

            BoardDto.BoardResponse dto = new BoardDto.BoardResponse();

            dto.setBoardId(post.getBoardId());

            dto.setTitle(post.getPostTitle());

// 🌟 수정 완료: LocalDateTime을 DTO에 할당 (DTO도 LocalDateTime이라고 가정)

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

                    commentDto.setUserName(comment.getUser().getUserName());

                    commentDto.setContents(comment.getCommentContents());

// 🌟 에러 94 해결 완료: LocalDateTime을 DTO에 할당

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

// 🌟 에러 112 해결 완료: Timestamp.valueOf() 없이 LocalDateTime.now() 사용

        comment.setCreatedAt(LocalDateTime.now());

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

// 🌟 수정 완료: LocalDateTime을 DTO에 할당

            dto.setCreatedAt(post.getCreatedAt());

            return dto;

        });

    }



    @Transactional

    public void reportPost(Integer boardId) {

        Board board = boardRepository.findById(boardId)

                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));



        board.setReportCount(board.getReportCount() + 1);

        boardRepository.save(board);



        if (board.getReportCount() >= REPORT_LIMIT) {

            boardRepository.delete(board);

        }

    }



    @Transactional
    public String reportComment(Integer commentId) { // 👈 void -> String 변경
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        comment.setReportCount(comment.getReportCount() + 1);

        if (comment.getReportCount() >= REPORT_LIMIT) {
            commentRepository.delete(comment);
            return "신고 누적으로 댓글이 삭제되었습니다.";
        } else {
            commentRepository.save(comment);
            return "댓글이 신고되었습니다.";
        }
    }



    @Transactional

    public void deletePost(Integer boardId) { // 👈 userId 파라미터 제거

// 1. 현재 로그인한 사용자 ID를 내부에서 직접 가져옵니다.

        String currentUserId = getCurrentUser().getUserId();



        Board board = boardRepository.findById(boardId)

                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));



// 2. 내부에서 가져온 ID로 비교합니다.

        if (!board.getUser().getUserId().equals(currentUserId)) {

            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");

        }

        boardRepository.delete(board);

    }



    @Transactional

    public void deleteComment(Integer commentId) { // 👈 userId 파라미터 제거

// 1. 현재 로그인한 사용자 ID를 내부에서 직접 가져옵니다.

        String currentUserId = getCurrentUser().getUserId();



        Comment comment = commentRepository.findById(commentId)

                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));



// 2. 내부에서 가져온 ID로 비교합니다.

        if (!comment.getUser().getUserId().equals(currentUserId)) {

            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");

        }

        commentRepository.delete(comment);

    }

}

