package com.data.ra.repository.candidate;

import com.data.ra.dto.admin.ApplicationDTO;
import com.data.ra.entity.admin.Application;
import com.data.ra.entity.admin.Page;

import java.util.Date;
import java.util.List;

public interface ApplicationRepository {
    List<Application> findAll();
    List<Application> findAllByUser(Integer candidateId);
    // Trả về danh sách ứng tuyển theo trang
    List<Application> findAllByUsers(Integer candidateId, int page, int size);

    // Trả về tổng số lượng ứng tuyển
    int countAllByUser(Integer candidateId);
    int countApplications();
    void save(Application application);
    void saveApp(Integer candidateId, Long recruitmentId, String cvUrl);
    boolean existsByCandidateIdAndRecruitmentId(Integer candidateId, Long recruitmentId);
    void markHandling(Integer id);
    void scheduleInterview(Integer id, Date interviewDate, String interviewLink);
    void approveInterview(Integer id);
    void saveInterviewResult(Integer id, String result, String resultNote);
    void destroyInterview(Integer id,String reason);
    Application findById(Integer id);
    Page<ApplicationDTO> findAllWithPaging(int page, int size, String keyword, String progress);
    Long countAll();
}
