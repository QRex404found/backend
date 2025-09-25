//DB:USER
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

    @Column(name = "USER_PW", nullable = false, length = 255)
    private String userPw;

    @Column(name = "PHONE", length = 20)
    private String phone;
}