//요청 헤더에서 JWT 토큰을 추출하고 유효성을 검사하는 필터를 정의

package com.found.qrex.security;

import com.found.qrex.service.TokenBlacklistService; // 🌟 1. TokenBlacklistService 임포트 추가
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // 🌟 (Optional) 더 안전한 문자열 검사를 위해 임포트 변경
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService; // 🌟 1. TokenBlacklistService 필드 추가

    // 🌟 2. 생성자 수정: TokenBlacklistService를 파라미터로 받아 주입
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        // 🌟 3. 블랙리스트 확인 로직 추가
        // 토큰이 존재하고, 그 토큰이 블랙리스트에 등록되어 있다면,
        if (token != null && tokenBlacklistService.isBlacklisted(token)) {
            // 인증을 거부하고 필터 체인을 중단합니다.
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
            return; // 여기서 필터 실행을 종료합니다.
        }

        // --- 이하 기존 로직은 그대로 유지 ---
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // (참고) getAuthentication(token)으로 변경하는 것이 더 일반적인 패턴입니다.
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // (참고) StringUtils.hasText()를 사용하는 것이 Null-safe 합니다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

