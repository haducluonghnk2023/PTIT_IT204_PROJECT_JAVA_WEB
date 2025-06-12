package com.data.ra.entity.admin;

import com.data.ra.entity.candidate.Candidate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Getter
@Setter
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "candidateId", nullable = false)
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "recruitmentPositionId", nullable = false)
    private RecruitmentPosition recruitmentPosition;

    @Column(nullable = false)
    private String cvUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('pending', 'handling', 'interviewing', 'done') DEFAULT 'pending'")
    private Progress progress = Progress.PENDING;

    private LocalDateTime interviewRequestDate;
    private String interviewRequestResult;
    private String interviewLink;
    private LocalDateTime interviewTime;
    private String interviewResult;

    @Column(columnDefinition = "TEXT")
    private String interviewResultNote;

    private LocalDateTime destroyAt;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String destroyReason;


    public enum Progress {
        PENDING,
        HANDLING,
        INTERVIEWING,
        DONE
    }
}