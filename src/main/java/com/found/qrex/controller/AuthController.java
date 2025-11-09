// /api/auth로 시작하는 로그인, 회원가입 관련 API를 담당

package com.found.qrex.controller;

import com.found.qrex.dto.AuthRequest;
import com.found.qrex.dto.UserResponse;
import com.found.qrex.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증/인가 API", description = "사용자 회원가입, 로그인 및 프로필 관련 API입니다.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<String> signUp(@RequestBody AuthRequest.SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    public ResponseEntity<UserResponse> login(@RequestBody AuthRequest.LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new UserResponse(token));
    }

    @PostMapping("/check-id")
    @Operation(summary = "ID 중복 확인", description = "회원가입 시 사용할 ID가 이미 존재하는지 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplication(
            @RequestBody AuthRequest.CheckIdRequest request
    ) {
        boolean isAvailable = authService.isIdAvailable(request.getUserId());
        Map<String, Boolean> response = Map.of("isAvailable", isAvailable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", description = "현재 로그인된 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AuthRequest.UpdateProfileRequest request) {

        authService.updateProfile(user.getUsername(), request);
        return ResponseEntity.ok("회원정보 수정이 완료되었습니다.");
    }

    @DeleteMapping("/profile")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 탈퇴시킵니다. 계정 삭제와 동시에 로그아웃 처리됩니다.")
    public ResponseEntity<String> deleteAccount(
            @AuthenticationPrincipal UserDetails user,
            HttpServletRequest request
    ) {
        authService.deleteAccountAndLogout(user.getUsername(), request);
        return ResponseEntity.ok("회원 탈퇴 및 로그아웃이 완료되었습니다.");
    }
}
