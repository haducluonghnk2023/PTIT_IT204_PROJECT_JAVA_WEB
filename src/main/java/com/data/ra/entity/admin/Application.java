package com.data.ra.entity.admin;

import com.data.ra.entity.candidate.Candidate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "application")
@Getter
@Setter
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidateId", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recruitmentPositionId", nullable = false)
    private RecruitmentPosition recruitmentPosition;

    @Column(nullable = false)
    private String cvUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Progress progress = Progress.pending;

    @Temporal(TemporalType.TIMESTAMP)
    private Date interviewRequestDate;

    private String interviewRequestResult;
    private String interviewLink;
    private String interviewResult;

    @Column
    private String interviewResultNote;

    @Temporal(TemporalType.TIMESTAMP)
    private Date destroyAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date interviewTime;

    @Column
    private String destroyReason;
}
