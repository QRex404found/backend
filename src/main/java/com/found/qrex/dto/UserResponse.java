// 로그인 성공 시 토큰 발급 등 사용자 관련 응답 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private String token;
}