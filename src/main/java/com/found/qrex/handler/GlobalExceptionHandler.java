// com.found.qrex.handler.GlobalExceptionHandler.java

package com.found.qrex.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🌟 IllegalArgumentException과 BadCredentialsException을 모두 처리합니다.
    @ExceptionHandler({IllegalArgumentException.class, BadCredentialsException.class})
    public ResponseEntity<String> handleAuthExceptions(RuntimeException ex) {

        // 인증 오류(ID/PW 불일치)는 401 Unauthorized 상태 코드를 반환합니다.
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // 예외 메시지 ("ID 또는 비밀번호가 잘못되었습니다." 등)를 클라이언트에게 응답 본문으로 전달합니다.
        String errorMessage = ex.getMessage();

        // 클라이언트에게 401 상태 코드와 메시지를 함께 보냅니다.
        return new ResponseEntity<>(errorMessage, status);
    }

    // 다른 예외 처리 핸들러는 여기에 추가할 수 있습니다.
}