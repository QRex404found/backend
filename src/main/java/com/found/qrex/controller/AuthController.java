// /api/auth로 시작하는 로그인, 회원가입 관련 API를 담당

package com.found.qrex.controller;

import com.found.qrex.dto.AuthRequest;
import com.found.qrex.dto.UserResponse;
import com.found.qrex.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody AuthRequest.SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody AuthRequest.LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new UserResponse(token));
    }

    // 추가적인 API 엔드포인트: check-id, google, kakao, profile update/delete
}