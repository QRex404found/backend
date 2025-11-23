package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.dto.AuthRequest;
import com.found.qrex.repository.UserRepository;
// 👇 추가된 리포지토리 import
import com.found.qrex.repository.BoardRepository;
import com.found.qrex.repository.CommentRepository;
import com.found.qrex.repository.AnalysisRepository;

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

    // 👇 추가된 필드들
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final AnalysisRepository analysisRepository;

    // 👇 생성자 수정 (리포지토리 주입)
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       TokenBlacklistService tokenBlacklistService,
                       BoardRepository boardRepository,
                       CommentRepository commentRepository,
                       AnalysisRepository analysisRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.analysisRepository = analysisRepository;
    }

    // ✅ 회원가입
    @Transactional
    public void signUp(AuthRequest.SignUpRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setUserName(request.getUserName());
        user.setUserPw(passwordEncoder.encode(request.getUserPw()));

        userRepository.save(user);
    }

    // ✅ 로그인
    public String login(AuthRequest.LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new BadCredentialsException("ID 또는 비밀번호가 잘못되었습니다.");
        }

        return jwtTokenProvider.generateToken(user.getUserId(), user.getUserName());
    }

    @Transactional(readOnly = true)
    public boolean isIdAvailable(String userId) {
        return !userRepository.existsByUserId(userId);
    }

    // ✅ 정보 수정
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

    // ✅ [핵심 수정] 회원 탈퇴 및 로그아웃
    @Transactional
    public void deleteAccountAndLogout(String userId, HttpServletRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + userId));

        // 1. 연관된 데이터를 '명시적으로' 먼저 삭제합니다. (외래 키 제약조건 해결)
        // 순서: 댓글 -> 게시글 -> 분석기록 -> 유저
        try {
            commentRepository.deleteByUser(user);
            boardRepository.deleteByUser(user);
            analysisRepository.deleteByUser(user);
        } catch (Exception e) {
            // 삭제 중 에러가 나도 일단 로그만 찍고 진행 시도 (혹은 여기서 throw 해도 됨)
            System.err.println("연관 데이터 삭제 중 경고: " + e.getMessage());
        }

        // 2. 이제 유저를 삭제합니다.
        userRepository.delete(user);

        // 3. 토큰 블랙리스트 처리
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