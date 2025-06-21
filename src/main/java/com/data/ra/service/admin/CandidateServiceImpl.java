package com.data.ra.service.admin;

import com.data.ra.entity.candidate.Candidate;
import com.data.ra.repository.admin.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CandidateServiceImpl implements  CandidateService {
    @Autowired
    private CandidateRepository candidateRepository;
    @Override
    public List<Candidate> findAllCandidates(int page, int size) {
        return candidateRepository.findAllCandidates(page, size);
    }

    @Override
    public void updateStatusById(Long id, boolean status) {
        candidateRepository.updateStatusById(id, status);
    }

    @Override
    public String resetPasswordById(Long id) {
        return candidateRepository.resetPasswordById(id);
    }

    @Override
    public List<Candidate> filterCandidates(String name, Integer experience, int minAge, int maxAge, String gender, String technology, int page, int size) {
        return candidateRepository.filterCandidates(name, experience, minAge, maxAge, gender, technology, page, size);
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        return candidateRepository.findById(id);
    }

    @Override
    public int countFilteredCandidates(String name, Integer experience, int minAge, int maxAge, String gender, String technology) {
        return candidateRepository.countFilteredCandidates(name, experience, minAge, maxAge, gender, technology);
    }

    @Override
    public Candidate findByUserId(Integer userId) {
        return candidateRepository.findByUserId(userId);
    }

    @Override
    public void save(Candidate candidate) {
        candidateRepository.save(candidate);
    }

    @Override
    public Candidate findByPhone(String phone) {
        return candidateRepository.findByPhone(phone);
    }
}
