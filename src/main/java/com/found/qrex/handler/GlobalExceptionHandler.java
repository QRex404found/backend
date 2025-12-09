package com.found.qrex.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ 잘못된 요청 / 삭제된 리소스 / 권한 오류 등
     * - 게시글 없음
     * - 댓글 없음
     * - 이미 삭제된 리소스 접근
     * - 본인 글 아님
     *
     * 👉 인증 오류 아님 ❌
     * 👉 400 Bad Request 로 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * ✅ 로그인 실패(ID / PW 불일치)만 401
     * 👉 이 경우에만 프론트에서 "토큰 만료 / 인증 실패"로 판단
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }
}
