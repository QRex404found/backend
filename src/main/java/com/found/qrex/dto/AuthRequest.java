// 로그인, 회원가입, 정보 수정 등 인증 관련 요청 데이터를 담습니다.
package com.found.qrex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @Getter
    @Setter
    public static class SignUpRequest {
        private String userId;
        private String userName;
        private String password;
        private String phone;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String userId;
        private String password;
    }

    @Getter
    @Setter
    public static class CheckIdRequest {
        private String userId;
    }

    @Getter
    @Setter
    public static class UpdateProfileRequest {
        private String newName;
        private String newPassword;
        private String verifyPassword;
    }
}