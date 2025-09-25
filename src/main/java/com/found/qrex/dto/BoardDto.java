// 게시글 작성 및 조회 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class BoardDto {
    @Getter
    @Setter
    public static class BoardResponse {
        private Integer boardId;
        private String title;
        private Timestamp createdAt;
    }

    @Getter
    @Setter
    public static class BoardDetailResponse {
        private Integer boardId;
        private String title;
        private String userId;
        private String imagePath;
        private String url;
        private String contents;
        private Timestamp createdAt;
        private List<CommentDto.CommentResponse> comments;
    }

    @Getter
    @Setter
    public static class BoardCreateRequest {
        private String postTitle;
        private String url;
        private String postContents;
        private String imagePath;
    }
}