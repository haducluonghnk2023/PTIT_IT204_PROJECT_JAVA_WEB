package com.data.ra.service.candidate;

import com.data.ra.entity.admin.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionService {
    List<RecruitmentPosition> findAlls(int page, int size);
    long countAll();
    List<RecruitmentPosition> findAll();
    List<RecruitmentPosition> searchByName(String name);
    List<RecruitmentPosition> filterByLocationTypeAndKeyword(String location, String type, String keyword);
    RecruitmentPosition findById(Long id);
    void save(RecruitmentPosition position);
    void deleteById(Long id);
    List<RecruitmentPosition> searchByName(String keyword, int page, int size);
    long countSearchByName(String keyword);
}
