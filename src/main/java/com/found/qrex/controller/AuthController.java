package com.found.qrex.controller;

import com.found.qrex.dto.AuthRequest;
import com.found.qrex.dto.UserResponse;
import com.found.qrex.security.JwtTokenProvider;
import com.found.qrex.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    // [중요] 토큰 재발급을 위해 Provider가 필요합니다.
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
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
    @Operation(summary = "프로필 수정", description = "현재 로그인된 사용자의 프로필 정보를 수정하고, 갱신된 정보가 담긴 새 토큰을 반환합니다.")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AuthRequest.UpdateProfileRequest request
    ) {
        // 1. 보여주신 AuthService 코드가 여기서 실행되어 DB를 바꿉니다.
        authService.updateProfile(user.getUsername(), request);

        // 2. DB는 바뀌었지만, 사용자가 가진 토큰은 아직 옛날 이름입니다.
        //    따라서 '새 이름'을 넣어서 토큰을 새로 찍어냅니다.
        //    (user.getUsername()은 ID이고, request.getNewName()이 바뀐 이름입니다)
        String newToken = jwtTokenProvider.generateToken(user.getUsername(), request.getNewName());

        // 3. 새 토큰을 프론트로 보냅니다.
        return ResponseEntity.ok(new UserResponse(newToken));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 탈퇴시킵니다.")
    public ResponseEntity<String> deleteAccount(
            Principal principal,
            HttpServletRequest request
    ) {
        authService.deleteAccountAndLogout(principal.getName(), request);
        return ResponseEntity.ok("회원 탈퇴 및 로그아웃이 완료되었습니다.");
    }
}