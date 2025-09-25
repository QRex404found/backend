// /api/auth로 시작하는 로그인, 회원가입 관련 API를 담당

package com.found.qrex.controller;

import com.found.qrex.dto.AuthRequest;
import com.found.qrex.dto.UserResponse;
import com.found.qrex.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody AuthRequest.UpdateProfileRequest request) {

        authService.updateProfile(userId, request); // userId를 넘겨주도록 서비스 수정 필요
        return ResponseEntity.ok("회원정보 수정이 완료되었습니다.");
    }

    @DeleteMapping("/profile")
    public ResponseEntity<String> deleteAccount(
            @AuthenticationPrincipal String userId,



            HttpServletRequest request // 🌟 현재 요청 객체를 직접 받아오도록 수정
    ) {
        // 🌟 사용자 ID와 요청 객체를 함께 서비스로 전달
        authService.deleteAccountAndLogout(userId, request);
        return ResponseEntity.ok("회원 탈퇴 및 로그아웃이 완료되었습니다.");
    }
}