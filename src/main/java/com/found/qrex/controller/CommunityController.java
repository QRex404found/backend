package com.found.qrex.controller;

import com.found.qrex.dto.BoardDto;
import com.found.qrex.dto.CommentDto;
import com.found.qrex.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

// ✅ [수정됨] 이 두 줄이 빠져서 에러가 났습니다. 꼭 포함되어야 합니다!
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@Tag(name = "커뮤니티 API", description = "커뮤니티 게시글 및 댓글 관련 API입니다.")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    @Operation(summary = "모든 게시글 목록 조회", description = "페이지네이션을 이용하여 모든 게시글 목록을 조회합니다.")
    public ResponseEntity<Page<BoardDto.BoardResponse>> getPosts(Pageable pageable) {
        Page<BoardDto.BoardResponse> posts = communityService.getPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{boardId}")
    @Operation(summary = "특정 게시글 상세 조회", description = "특정 게시글 ID에 해당하는 게시글의 상세 내용을 조회합니다.")
    public ResponseEntity<BoardDto.BoardDetailResponse> getPostDetail(@PathVariable Integer boardId) {
        BoardDto.BoardDetailResponse post = communityService.getPostDetail(boardId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    @Operation(summary = "새 게시글 작성", description = "새로운 게시글을 작성합니다. (FormData 사용)")
    public ResponseEntity<?> createPost(
            @RequestParam("postTitle") String postTitle,
            @RequestParam("postContents") String postContents,
            @RequestParam(value = "url", required = false) String url,
            @RequestPart(value = "photoFile", required = false) MultipartFile photoFile
    ) {
        BoardDto.BoardCreateRequest request = new BoardDto.BoardCreateRequest();
        request.setPostTitle(postTitle);
        request.setPostContents(postContents);
        request.setUrl(url);

        communityService.createPost(request, photoFile);

        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    @PostMapping("/posts/{boardId}/comments")
    @Operation(summary = "댓글 추가", description = "특정 게시글에 댓글을 추가합니다.")
    public ResponseEntity<?> addComment(@PathVariable Integer boardId, @RequestBody CommentDto.CommentRequest request) {
        communityService.addComment(boardId, request);
        return ResponseEntity.ok("댓글이 성공적으로 추가되었습니다.");
    }

    @GetMapping("/myposts")
    @Operation(summary = "내가 작성한 게시글 목록 조회", description = "사용자가 작성한 게시글 목록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<Page<BoardDto.BoardResponse>> getMyPosts(Pageable pageable) {
        Page<BoardDto.BoardResponse> myPosts = communityService.getMyPosts(pageable);
        return ResponseEntity.ok(myPosts);
    }

    @PostMapping("/posts/{boardId}/report")
    @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다.")
    public ResponseEntity<String> reportPost(@PathVariable Integer boardId) {
        communityService.reportPost(boardId);
        return ResponseEntity.ok("게시글이 신고되었습니다.");
    }

    // ✅ [JSON 응답으로 변경됨]
    @PostMapping("/comments/{commentId}/report")
    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다.")
    public ResponseEntity<Map<String, String>> reportComment(@PathVariable Integer commentId) {
        String resultMessage = communityService.reportComment(commentId);
        return ResponseEntity.ok(Collections.singletonMap("message", resultMessage));
    }

    @DeleteMapping("/posts/{boardId}")
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    public ResponseEntity<String> deletePost(@PathVariable Integer boardId) {
        communityService.deletePost(boardId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    public ResponseEntity<String> deleteComment(@PathVariable Integer commentId) {
        communityService.deleteComment(commentId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}