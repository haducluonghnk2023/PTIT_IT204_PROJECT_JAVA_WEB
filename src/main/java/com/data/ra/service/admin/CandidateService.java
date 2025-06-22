package com.data.ra.service.admin;

import com.data.ra.entity.candidate.Candidate;

import java.util.List;
import java.util.Optional;

public interface CandidateService {
    List<Candidate> findAllCandidates(int page, int size);

    void updateStatusById(Long id, boolean status);

    String resetPasswordById(Long id);

    List<Candidate> filterCandidates(String name, Integer experience, int minAge, int maxAge, String gender, String technology, int page, int size);

    Optional<Candidate> findById(Long id);

    int countFilteredCandidates(String name, Integer experience, int minAge, int maxAge,
                                String gender, String technology);

    Candidate findByUserId(Integer userId);

    void save(Candidate candidate);

    Candidate findByPhone(String phone);

    Long countAll();
}
