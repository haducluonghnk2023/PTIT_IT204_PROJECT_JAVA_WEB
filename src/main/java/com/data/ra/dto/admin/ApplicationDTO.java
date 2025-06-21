package com.data.ra.dto.admin;

import com.data.ra.entity.admin.Application;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

@Setter
@Getter
public class ApplicationDTO {
    private Integer id;
    private String candidateName;
    private String recruitmentName;
    private String cvUrl;
    private MultipartFile cvFile;

    private String progress;
    private String createAt;
    private String updateAt;
    private String interviewLink;
    private Date interviewTime;
    private String interviewResult;
    private String interviewResultNote;

    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ApplicationDTO(Application app) {
        this.id = app.getId();
        this.cvUrl = app.getCvUrl();
        this.progress = app.getProgress() != null ? app.getProgress().name() : "pending";

        this.candidateName = app.getCandidate() != null ? app.getCandidate().getName() : "N/A";
        this.recruitmentName = app.getRecruitmentPosition() != null ? app.getRecruitmentPosition().getName() : "N/A";

        this.createAt = formatDisplayDate(app.getCreateAt());
        this.updateAt = formatDisplayDate(app.getUpdateAt());

        this.interviewTime = app.getInterviewTime();
        this.interviewLink = app.getInterviewLink();
        this.interviewResult = app.getInterviewResult() != null ? app.getInterviewResult() : "";
        this.interviewResultNote = app.getInterviewResultNote() != null ? app.getInterviewResultNote() : "";
    }

    private String formatDisplayDate(Date date) {
        return date != null ? displayFormat.format(date) : "";
    }

    public ApplicationDTO() {}
}
