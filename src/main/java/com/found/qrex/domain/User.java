package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "USER_ID", length = 45)
    private String userId;

    @Column(name = "USER_NAME", nullable = false, length = 45)
    private String userName;

    /* 1. (복구) 필드명을 원래대로 'userPw'로 유지합니다. */
    @Column(name = "USER_PW", nullable = false, length = 255)
    private String userPw; // 👈 password -> userPw 로 복구

    /* 2. (수정) 전화번호 NULL 허용: 회원가입 시 빈 문자열/null 허용 */
    @Column(name = "PHONE", nullable = true, length = 20)
    private String phone; // 👈 nullable = true 추가
}