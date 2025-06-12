package com.data.ra.entity.admin;

import com.data.ra.entity.candidate.Candidate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "technology")
@Getter
@Setter
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "technologies")
    private Set<Candidate> candidates;

    @ManyToMany(mappedBy = "technologies")
    private Set<RecruitmentPosition> recruitmentPositions;

}