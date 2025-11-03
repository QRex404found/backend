package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.dto.AuthRequest;
import com.found.qrex.repository.UserRepository;
import com.found.qrex.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // --- 회원가입 (수정 필요 부분 1) ---
    @Transactional
    public void signUp(AuthRequest.SignUpRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setUserName(request.getUserName());

        // 🌟 수정: request.getPassword() -> request.getUserPw()
        user.setUserPw(passwordEncoder.encode(request.getUserPw()));

        user.setPhone(request.getPhone());
        userRepository.save(user);
    }

    // --- 로그인 (수정 필요 부분 2) ---
    public String login(AuthRequest.LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다."));

        // 🌟 수정: request.getPassword()는 LoginRequest의 메서드이므로 유지해야 함
        // LoginRequest DTO는 password 필드를 그대로 쓰고 있으므로 getUserPw()가 아닌 getPassword()가 맞습니다.
        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다.");
        }
        return jwtTokenProvider.generateToken(user.getUserId());
    }

    @Transactional(readOnly = true)
    public boolean isIdAvailable(String userId) {
        return !userRepository.existsByUserId(userId);
    }

    // --- 정보 수정 (수정 필요 부분 3) ---
    @Transactional
    public void updateProfile(String userId, AuthRequest.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
        if (request.getNewName() != null && !request.getNewName().isBlank()) {
            user.setUserName(request.getNewName());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!request.getNewPassword().equals(request.getVerifyPassword())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }

            // 🌟 수정: request.getNewPassword()는 UpdateProfileRequest의 메서드이므로 유지해야 함
            user.setUserPw(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    @Transactional
    public void deleteAccountAndLogout(String userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + userId));
        userRepository.delete(user);

        String token = resolveToken(request);

        if (token != null) {
            tokenBlacklistService.blacklist(token);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
