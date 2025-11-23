// DB: BOARD
package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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

    // ⭐ URL 저장 필드 (문제 없음!)
    @Column(name = "URL", length = 2083)
    private String url;

    @Lob
    @Column(name = "POST_CONTENTS", columnDefinition = "TEXT", nullable = false)
    private String postContents;

    @Column(name = "REPORT_COUNT", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
