package com.data.ra.service.candidate;

import com.data.ra.entity.admin.Application;
import com.data.ra.repository.candidate.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class ApplicationServiceImpl implements  ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;


    @Override
    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    @Override
    public List<Application> findAllByUser(Integer candidateId) {
        return applicationRepository.findAllByUser(candidateId);
    }

    @Override
    public List<Application> findAllByUsers(Integer candidateId, int page, int size) {
        return applicationRepository.findAllByUsers(candidateId, page, size);
    }

    @Override
    public int countAllByUser(Integer candidateId) {
        return applicationRepository.countAllByUser(candidateId);
    }


    @Override
    public int countApplications() {
        return applicationRepository.countApplications();
    }

    @Override
    public void save(Application application) {
        applicationRepository.save(application);
    }

    @Override
    public void saveApp(Integer candidateId, Long recruitmentId, String cvUrl) {
        applicationRepository.saveApp(candidateId, recruitmentId, cvUrl);
    }

    @Override
    public boolean existsByCandidateIdAndRecruitmentId(Integer candidateId, Long recruitmentId) {
        return applicationRepository.existsByCandidateIdAndRecruitmentId(candidateId, recruitmentId);
    }

    @Override
    public void markHandling(Integer id) {
        applicationRepository.markHandling(id);
    }

    @Override
    public void scheduleInterview(Integer id, Date interviewDate, String interviewLink) throws IllegalStateException {
        applicationRepository.scheduleInterview(id, interviewDate, interviewLink);
    }

    @Override
    public void approveInterview(Integer id) {
        applicationRepository.approveInterview(id);
    }

    @Override
    public void saveInterviewResult(Integer id, String result, String resultNote) {
        applicationRepository.saveInterviewResult(id, result, resultNote);
    }

    @Override
    public void destroyInterview(Integer id,String reason) {
        applicationRepository.destroyInterview(id,reason);
    }

    @Override
    public Application findById(Integer id) {
        return applicationRepository.findById(id);
    }
}
