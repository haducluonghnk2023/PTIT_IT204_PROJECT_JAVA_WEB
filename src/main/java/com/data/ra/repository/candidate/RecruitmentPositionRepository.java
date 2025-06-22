package com.data.ra.repository.candidate;

import com.data.ra.entity.admin.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionRepository {
    List<RecruitmentPosition> findAlls(int page, int size);
    long countAll();
    List<RecruitmentPosition> findAll();
    List<RecruitmentPosition> findByNameContainingIgnoreCase(String name);
    List<RecruitmentPosition> filterByLocationTypeAndKeyword(String location, String type, String keyword);
    RecruitmentPosition findById(Long id);
    void save(RecruitmentPosition position);
    void deleteById(Long id);
    List<RecruitmentPosition> searchByName(String keyword, int page, int size);
    long countSearchByName(String keyword);
    RecruitmentPosition findByName(String name);

}
