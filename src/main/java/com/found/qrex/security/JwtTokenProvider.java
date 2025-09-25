package com.found.qrex.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration.access}") long accessTokenExpiration) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        // 최신 JJWT 빌더 패턴을 사용하여 경고를 제거
        return Jwts.builder()
                .claims(Map.of("sub", userId)) // setSubject 대신 claims 사용
                .issuedAt(now) // setIssuedAt 대신
                .expiration(expiration) // setExpiration 대신
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        try {
            // Jwts.parserBuilder()가 정상적으로 동작하도록 예외 처리 추가
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKeySpec) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 subject를 얻을 수 있도록 수정
            return e.getClaims().getSubject();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKeySpec) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}