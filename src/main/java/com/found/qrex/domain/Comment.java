//DB:COMMENT
package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID", nullable = false)
    private Integer commentId;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID", referencedColumnName = "BOARD_ID")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private User user;

    @Lob
    @Column(name = "COMMENT_CONTENTS", columnDefinition = "TEXT", nullable = false)
    private String commentContents;

    @Column(name = "REPORT_COUNT", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}