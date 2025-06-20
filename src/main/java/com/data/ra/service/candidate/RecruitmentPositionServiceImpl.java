package com.data.ra.service.candidate;

import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.repository.candidate.RecruitmentPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RecruitmentPositionServiceImpl implements  RecruitmentPositionService {
    @Autowired
    private RecruitmentPositionRepository recruitmentPositionRepository;

    @Override
    public List<RecruitmentPosition> findAlls(int page, int size) {
        return recruitmentPositionRepository.findAlls(page, size);
    }

    @Override
    public long countAll() {
        return recruitmentPositionRepository.countAll();
    }

    @Override
    public List<RecruitmentPosition> findAll() {
        return recruitmentPositionRepository.findAll();
    }

    @Override
    public List<RecruitmentPosition> searchByName(String name) {
        return recruitmentPositionRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<RecruitmentPosition> filterByLocationTypeAndKeyword(String location, String type, String keyword) {
        return  recruitmentPositionRepository.filterByLocationTypeAndKeyword(location, type, keyword);
    }

    @Override
    public RecruitmentPosition findById(Long id) {
        return recruitmentPositionRepository.findById(id);
    }

    @Override
    public void save(RecruitmentPosition position) {
        recruitmentPositionRepository.save(position);
    }

    @Override
    public void deleteById(Long id) {
        recruitmentPositionRepository.deleteById(id);
    }

    @Override
    public List<RecruitmentPosition> searchByName(String keyword, int page, int size) {
        return recruitmentPositionRepository.searchByName(keyword, page, size);
    }

    @Override
    public long countSearchByName(String keyword) {
        return recruitmentPositionRepository.countSearchByName(keyword);
    }


}
