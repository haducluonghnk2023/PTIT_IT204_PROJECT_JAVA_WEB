package com.data.ra.entity.admin;

import com.data.ra.entity.candidate.Candidate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "technology")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private Boolean is_deleted = false;
    @ManyToMany(mappedBy = "technologies",fetch =  FetchType.EAGER)
    private Set<Candidate> candidates;

    @ManyToMany(mappedBy = "technologies",fetch = FetchType.EAGER)
    private Set<RecruitmentPosition> recruitmentPositions;

}