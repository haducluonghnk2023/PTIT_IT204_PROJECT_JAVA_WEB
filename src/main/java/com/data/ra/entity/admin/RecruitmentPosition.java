package com.data.ra.entity.admin;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "recruitment_position")
@Getter
@Setter
public class RecruitmentPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal minSalary;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxSalary;

    @Column(nullable = false)
    private Integer minExperience = 0;

    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(nullable = false)
    private LocalDate expiredDate;

    @ManyToMany
    @JoinTable(
            name = "recruitment_position_technology",
            joinColumns = @JoinColumn(name = "recruitmentPositionId"),
            inverseJoinColumns = @JoinColumn(name = "technologyId")
    )
    private Set<Technology> technologies;

    @OneToMany(mappedBy = "recruitmentPosition", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.data.ra.entity.admin.Application> applications;
}
