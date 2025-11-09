// package com.found.qrex.service;

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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ✅ 회원가입 (변경 없음)
    @Transactional
    public void signUp(AuthRequest.SignUpRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setUserName(request.getUserName());
        user.setUserPw(passwordEncoder.encode(request.getUserPw())); // 패스워드 암호화 저장

        userRepository.save(user);
    }

    // ✅ 로그인 (수정됨)
    public String login(AuthRequest.LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다.");
        }

        // ⭐️ [핵심 수정]
        // user.getUserId()만 넘기는 대신, user.getUserName()도 함께 넘깁니다.
        return jwtTokenProvider.generateToken(user.getUserId(), user.getUserName());
    }

    @Transactional(readOnly = true)
    public boolean isIdAvailable(String userId) {
        return !userRepository.existsByUserId(userId);
    }

    // ✅ 정보 수정 (변경 없음)
    @Transactional
    public void updateProfile(String userId, AuthRequest.UpdateProfileRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        if (request.getNewName() != null && !request.getNewName().isBlank()) {
            user.setUserName(request.getNewName());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!request.getNewPassword().equals(request.getVerifyPassword())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            user.setUserPw(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // ✅ 회원 탈퇴 (변경 없음)
    @Transactional
    public void deleteAccountAndLogout(String userId, HttpServletRequest request) {
        User user = userRepository.findByUserId(userId)
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