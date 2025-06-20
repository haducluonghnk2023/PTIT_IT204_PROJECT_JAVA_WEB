package com.data.ra.service.admin;

import com.data.ra.entity.admin.Technology;
import com.data.ra.repository.admin.TechnologyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TechnologyServiceImpl implements TechnologyService {
    @Autowired
    private TechnologyRepository technologyRepository;
    @Override
    public List<Technology> findAll(int page, int size, String search) {
        return technologyRepository.findAll(page, size, search);
    }

    @Override
    public List<Technology> findAlls() {
        return technologyRepository.findAlls();
    }

    @Override
    public Technology findById(Long id) {
        return technologyRepository.findById(id);
    }

    @Override
    public void save(Technology technology) {
        technologyRepository.save(technology);
    }

    @Override
    public void deleteById(Long id) {
        technologyRepository.deleteById(id);
    }

    @Override
    public long count(String search) {
        return technologyRepository.count(search);
    }

    @Override
    public boolean isNameDuplicate(String name, Long currentId) {
        Technology existing = technologyRepository.findByName(name.trim());
        return existing != null && !existing.getId().equals(currentId);
    }

    @Override
    public List<Technology> findByIds(List<Long> ids) {
        return technologyRepository.findByIds(ids);
    }
}
