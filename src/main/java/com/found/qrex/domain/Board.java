//DB:BOARD
package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

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

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}