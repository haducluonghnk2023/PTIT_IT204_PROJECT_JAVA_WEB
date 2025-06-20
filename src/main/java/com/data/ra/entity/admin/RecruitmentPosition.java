package com.data.ra.entity.admin;
import com.data.ra.entity.admin.Application;
import com.data.ra.entity.admin.Technology;
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
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

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

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 20)
    private String education;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recruitment_position_technology",
            joinColumns = @JoinColumn(name = "recruitmentPositionId"),
            inverseJoinColumns = @JoinColumn(name = "technologyId")
    )
    private Set<Technology> technologies; // ✅ dùng Set thay vì List

    @OneToMany(mappedBy = "recruitmentPosition", cascade = CascadeType.ALL, orphanRemoval = true,fetch =  FetchType.EAGER)
    private Set<Application> applications;

    public String getSalaryRange() {
        int min = minSalary.intValue();
        int max = maxSalary.intValue();
        return min + "USD - " + max + "USD";
    }

    public String getTimeAgo() {
        if (createdDate == null) return "";

        java.util.Date now = new java.util.Date();
        java.util.Date created = java.sql.Date.valueOf(createdDate);

        long diffMillis = now.getTime() - created.getTime();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);
        long diffMonths = diffDays / 30;

        if (diffDays < 1) return "Today";
        if (diffDays < 30) return diffDays + " days ago";
        return diffMonths + " months ago";
    }
}
