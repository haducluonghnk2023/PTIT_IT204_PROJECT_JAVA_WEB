package com.data.ra.dto.admin;

import com.data.ra.entity.admin.Technology;
import com.data.ra.entity.candidate.Candidate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CandidateDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private int experience;
    private String gender;
    private String status;
    private LocalDate dob;

    private String firstTechnology; // công nghệ đầu tiên
    private List<String> remainingTechnologies = new ArrayList<>(); // danh sách còn lại

    public CandidateDTO(Candidate candidate) {
        this.id = candidate.getId();
        this.name = candidate.getName();
        this.email = candidate.getEmail();
        this.phone = candidate.getPhone();
        this.experience = candidate.getExperience();
        this.gender = candidate.getGender();
        this.status = candidate.getStatus();
        this.dob = candidate.getDob();

        Set<Technology> techs = candidate.getTechnologies();
        if (techs != null && !techs.isEmpty()) {
            List<Technology> techList = new ArrayList<>(techs);
            techList.sort((a, b) -> a.getId().compareTo(b.getId()));

            // Nếu có ít nhất 1
            if (techList.size() >= 1) {
                this.firstTechnology = techList.get(0).getName();
            }

            // Nếu có ít nhất 2
            if (techList.size() >= 2) {
                this.remainingTechnologies.add(techList.get(1).getName()); // tech thứ 2
            }

            // Nếu có nhiều hơn 2, thêm các tech còn lại vào danh sách
            for (int i = 2; i < techList.size(); i++) {
                this.remainingTechnologies.add(techList.get(i).getName());
            }
        }
    }

}
