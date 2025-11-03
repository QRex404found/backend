// 게시글 작성 및 조회 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

// import java.sql.Timestamp; // 🚨 Timestamp 임포트를 삭제하고
import java.time.LocalDateTime; // <-- LocalDateTime 임포트를 추가합니다.
import java.util.List;

@Getter
@Setter
public class BoardDto {

    // 1. BoardResponse (게시글 목록 조회)
    @Getter
    @Setter
    public static class BoardResponse {
        private Integer boardId;
        private String title;
        // 🌟 수정: Timestamp -> LocalDateTime
        private LocalDateTime createdAt;
    }

    // 2. BoardDetailResponse (게시글 상세 조회)
    @Getter
    @Setter
    public static class BoardDetailResponse {
        private Integer boardId;
        private String title;
        private String userId;
        private String imagePath;
        private String url;
        private String contents;
        // 🌟 수정: Timestamp -> LocalDateTime
        private LocalDateTime createdAt;
        private List<CommentDto.CommentResponse> comments;
    }

    // 3. BoardCreateRequest (게시글 작성 요청)
    @Getter
    @Setter
    public static class BoardCreateRequest {
        private String postTitle;
        private String url;
        private String postContents;
        private String imagePath;
    }
}