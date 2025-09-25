// 댓글 작성 및 조회 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CommentDto {
    @Getter
    @Setter
    public static class CommentResponse {
        private Integer commentId;
        private String userId;
        private String contents;
        private Timestamp createdAt;
    }

    @Getter
    @Setter
    public static class CommentRequest {
        private String contents;
    }
}