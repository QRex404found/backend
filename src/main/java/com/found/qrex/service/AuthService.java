// 로그인, 회원가입, 구글/카카오 로그인, 회원 정보 수정/탈퇴 등의 로직을 처리합니다.
package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.dto.AuthRequest;
import com.found.qrex.repository.UserRepository;
import com.found.qrex.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void signUp(AuthRequest.SignUpRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setUserName(request.getUserName());
        user.setUserPw(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        userRepository.save(user);
    }

    public String login(AuthRequest.LoginRequest request) {
        // 'user == null' 조건 경고 및 호환되지 않는 타입 오류 수정
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("ID 또는 비밀번호가 잘못되었습니다."));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new IllegalArgumentException("ID 또는 비밀번호가 잘못되었습니다.");
        }
        return jwtTokenProvider.generateToken(user.getUserId());
    }

    public boolean isIdAvailable(String userId) {
        return !userRepository.existsByUserId(userId);
    }

    @Transactional
    public void updateProfile(AuthRequest.UpdateProfileRequest request) {
        // 현재 로그인된 사용자 ID를 SecurityContextHolder에서 가져옴
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setUserPw(passwordEncoder.encode(request.getNewPassword()));
        }
        if (request.getNewPhone() != null && !request.getNewPhone().isEmpty()) {
            user.setPhone(request.getNewPhone());
        }
        userRepository.save(user);
    }

    // deleteAccount 메서드 수정 및 사용
    @Transactional
    public void deleteAccount() {
        // 현재 로그인된 사용자 ID를 SecurityContextHolder에서 가져옴
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
    }
}