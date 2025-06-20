package com.data.ra.dto.admin;

import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.entity.admin.Technology;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class RecruitmentPositionDTO {
    private Long id;
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiredDate;

    private String createdDateStr;
    private List<Long> technologyIds = new ArrayList<>();
    private List<Technology> technologiesHtml;
    private String timeAgo;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private int minExperience;
    private String description;
    private String requirements;
    private String education;
    private String location;
    private String type;

    private String firstTechnology;
    private List<String> remainingTechnologies = new ArrayList<>();

    // Default constructor
    public RecruitmentPositionDTO() {
    }

    // Constructor from entity
    public RecruitmentPositionDTO(RecruitmentPosition recruitmentPosition) {
        this.id = recruitmentPosition.getId();
        this.name = recruitmentPosition.getName();
        this.createdDate = recruitmentPosition.getCreatedDate();
        this.expiredDate = recruitmentPosition.getExpiredDate();
        this.timeAgo = recruitmentPosition.getTimeAgo();
        this.minSalary = recruitmentPosition.getMinSalary();
        this.maxSalary = recruitmentPosition.getMaxSalary();
        this.minExperience = recruitmentPosition.getMinExperience();
        this.description = recruitmentPosition.getDescription();
        this.requirements = recruitmentPosition.getRequirements();
        this.education = recruitmentPosition.getEducation();
        this.location = recruitmentPosition.getLocation();
        this.type = recruitmentPosition.getType();

        if (this.createdDate != null) {
            this.createdDateStr = this.createdDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        List<Technology> techs = recruitmentPosition.getTechnologies().stream()
                .sorted(Comparator.comparingLong(Technology::getId))
                .collect(Collectors.toList());

        this.technologiesHtml = techs;
        this.technologyIds = techs.stream()
                .map(Technology::getId)
                .collect(Collectors.toList());

        if (!techs.isEmpty()) {
            this.firstTechnology = techs.get(0).getName();
            this.remainingTechnologies = techs.stream()
                    .skip(1)
                    .map(Technology::getName)
                    .collect(Collectors.toList());
        }
    }
}