package com.found.qrex.controller;

import com.found.qrex.domain.Board;
import com.found.qrex.domain.User;
import com.found.qrex.dto.PostRequest;
import com.found.qrex.repository.BoardRepository;
import com.found.qrex.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PostController(BoardRepository boardRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===========================================================
    // 1. 게시글 검색
    // ===========================================================
    @GetMapping("/search")
    public List<String> searchPosts(@RequestParam("keyword") String keyword) {
        return boardRepository.findByPostTitleContainingIgnoreCaseOrPostContentsContaining(keyword, keyword)
                .stream()
                .map(board -> "[ID:" + board.getBoardId() + "] 제목: " + board.getPostTitle() +
                        " / 내용: " + board.getPostContents())
                .toList();
    }

    // ===========================================================
    // 2. 게시글 작성
    // ===========================================================
    @PostMapping
    public String createPost(@RequestBody PostRequest request) {

        User author = userRepository.findByUserId(request.getWriterId())
                .orElseGet(() -> userRepository.findByUserId("AI_AGENT").orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUserId("AI_AGENT");
                    newUser.setUserName("QRex AI System");
                    newUser.setUserPw(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return userRepository.save(newUser);
                }));

        Board board = new Board();
        board.setPostTitle(request.getTitle());
        board.setPostContents(request.getContent());
        board.setUrl(request.getUrl());
        board.setUser(author);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        board.setImagePath("");

        boardRepository.save(board);
        return "게시글 저장 완료!";
    }

    // ===========================================================
    // 3. 🔥 같은 제목의 내 게시글 목록 조회 (AI 선택용)
    // ===========================================================
    @GetMapping("/myPostsByTitle")
    public List<MyPostPreviewResponse> myPostsByTitle(
            @RequestParam String title,
            @RequestParam String requesterId,
            @RequestParam(defaultValue = "false") boolean exact
    ) {
        List<Board> targets;

        if (exact) {
            targets = boardRepository.findByPostTitleIgnoreCase(title);
        } else {
            targets = boardRepository.findByPostTitleContainingIgnoreCase(title);
        }

        return targets.stream()
                .filter(post -> post.getUser().getUserId().equals(requesterId))
                .map(post -> new MyPostPreviewResponse(
                        post.getBoardId(),
                        post.getPostTitle(),                     // ⭐ 제목 추가!!!
                        createPreview(post.getPostContents())
                ))
                .toList();
    }

    // 🆕 내용 미리보기 생성 (20자 제한)
    private String createPreview(String content) {
        if (content == null) return "";
        content = content.trim();
        return content.length() > 20 ? content.substring(0, 20) + "…" : content;
    }

    // ===========================================================
    // ⭐ 수정된 응답 DTO — title 필드를 포함하도록 변경
    // ===========================================================
    public record MyPostPreviewResponse(
            Integer postId,
            String title,           // 🔥 꼭 필요했던 필드!
            String contentPreview
    ) {}

    // ===========================================================
    // 4. 게시글 ID 기반 삭제
    // ===========================================================
    @Transactional
    @PostMapping("/deleteById")
    public String deletePostById(@RequestBody Map<String, Object> body) {

        Integer postId = (body.get("postId") instanceof Number num)
                ? num.intValue()
                : Integer.valueOf(body.get("postId").toString());

        String requesterId = body.get("requesterId").toString();

        Board board = boardRepository.findById(postId).orElse(null);

        if (board == null) return "삭제 실패: 게시글을 찾을 수 없습니다.";

        if (!board.getUser().getUserId().equals(requesterId)) {
            return "삭제 실패: 다른 사용자의 게시글은 삭제할 수 없습니다.";
        }

        boardRepository.delete(board);
        return "게시글이 성공적으로 삭제되었습니다.";
    }
}
