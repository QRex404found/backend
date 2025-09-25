package com.found.qrex.controller;

import com.found.qrex.dto.BoardDto;
import com.found.qrex.dto.CommentDto;
import com.found.qrex.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<BoardDto.BoardResponse>> getPosts(Pageable pageable) {
        Page<BoardDto.BoardResponse> posts = communityService.getPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{boardId}")
    public ResponseEntity<BoardDto.BoardDetailResponse> getPostDetail(@PathVariable Integer boardId) {
        BoardDto.BoardDetailResponse post = communityService.getPostDetail(boardId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody BoardDto.BoardCreateRequest request) {
        communityService.createPost(request);
        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    // `addComment` 메서드 추가
    @PostMapping("/posts/{boardId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Integer boardId, @RequestBody CommentDto.CommentRequest request) {
        communityService.addComment(boardId, request);
        return ResponseEntity.ok("댓글이 성공적으로 추가되었습니다.");
    }

    // `getMyPosts` 메서드 추가
    @GetMapping("/myposts")
    public ResponseEntity<Page<BoardDto.BoardResponse>> getMyPosts(Pageable pageable) {
        Page<BoardDto.BoardResponse> myPosts = communityService.getMyPosts(pageable);
        return ResponseEntity.ok(myPosts);
    }
}