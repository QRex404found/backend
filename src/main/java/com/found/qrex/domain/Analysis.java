//DB: ANALYSIS
package com.found.qrex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

@Entity
@Table(name = "ANALYSIS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ANALYSIS_ID", nullable = false)
    private Integer analysisId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private User user;

    @Column(name = "SCAN_RESULT", length = 25)
    private String scanResult;

    @Column(name = "ANALYSIS_URL", length = 2083)
    private String analysisUrl;

    @Column(name = "ANALYSIS_TITLE", length = 255)
    private String analysisTitle;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}