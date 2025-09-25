package com.found.qrex.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 로그아웃되거나 무효화된 토큰을 관리하는 블랙리스트 서비스입니다.
 */
@Service
public class TokenBlacklistService {

    // 동시성 문제를 고려하여 스레드 안전한 Set을 사용합니다.
    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    /**
     * 토큰을 블랙리스트에 추가합니다.
     * @param token 무효화할 토큰
     */
    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * 해당 토큰이 블랙리스트에 포함되어 있는지 확인합니다.
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 아니면 false
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
