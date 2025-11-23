// 댓글 작성 및 조회 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

// import java.sql.Timestamp; // 🚨 이 임포트 라인을 삭제합니다.
import java.time.LocalDateTime; // <-- LocalDateTime 임포트 추가

@Getter
@Setter
public class CommentDto {
    @Getter
    @Setter
    public static class CommentResponse {
        private Integer commentId;
        private String userId;
        private String userName;
        private String contents;
        // 🌟 수정: Timestamp 대신 LocalDateTime 사용
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    public static class CommentRequest {
        private String contents;
    }
}