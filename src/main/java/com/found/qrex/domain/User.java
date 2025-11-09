package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "userId")
public class User {
    @Id
    @Column(name = "USER_ID", length = 45)
    private String userId;

    @Column(name = "USER_NAME", nullable = false, length = 45)
    private String userName;

    /* 1. (복구) 필드명을 원래대로 'userPw'로 유지합니다. */
    @Column(name = "USER_PW", nullable = false, length = 255)
    private String userPw;

    // ⭐⭐⭐ 추가된 부분: Email 필드 ⭐⭐⭐
    // 소셜 로그인은 이메일을 필수로 제공하지 않을 수 있으므로, nullable을 true로 설정하는 것이 안전합니다.
    @Column(name = "EMAIL", length = 45)
    private String email;
}