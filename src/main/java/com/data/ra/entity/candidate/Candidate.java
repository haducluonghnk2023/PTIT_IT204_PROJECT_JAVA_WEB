package com.data.ra.entity.candidate;

import com.data.ra.entity.admin.Application;
import com.data.ra.entity.admin.Technology;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "candidate")
@Getter
@Setter
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String phone;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int experience;

    @Column(length = 10)
    private String gender;

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Active'")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dob;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private Set<Application> applications;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "candidate_technology",
            joinColumns = @JoinColumn(name = "candidateId"),
            inverseJoinColumns = @JoinColumn(name = "technologyId")
    )
    private Set<Technology> technologies;

}
