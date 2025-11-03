//DB:BOARD
package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// import java.sql.Timestamp; // 🚨 Timestamp 임포트를 삭제합니다.
import java.time.LocalDateTime; // <-- LocalDateTime 임포트를 추가합니다.

@Entity
@Table(name = "BOARD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOARD_ID", nullable = false)
    private Integer boardId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private User user;

    @Column(name = "POST_TITLE", length = 255, nullable = false)
    private String postTitle;

    @Column(name = "IMAGE_PATH", length = 255)
    private String imagePath;

    @Column(name = "URL", length = 2083)
    private String url;

    @Lob
    @Column(name = "POST_CONTENTS", columnDefinition = "TEXT", nullable = false)
    private String postContents;

    @Column(name = "REPORT_COUNT", nullable = false)
    private Integer reportCount = 0;

    // 🌟 수정: Timestamp 대신 LocalDateTime 사용
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 🌟 수정: Timestamp 대신 LocalDateTime 사용
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}