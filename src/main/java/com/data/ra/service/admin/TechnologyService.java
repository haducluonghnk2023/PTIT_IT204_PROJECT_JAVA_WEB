package com.data.ra.service.admin;

import com.data.ra.entity.admin.Technology;

import java.util.List;

public interface TechnologyService {
    List<Technology> findAll(int page, int size, String search);
    List<Technology> findAlls();
    Technology findById(Long id);
    void save(Technology technology);
    void deleteById(Long id);
    long count(String search);
    boolean isNameDuplicate(String name, Long currentId);
    List<Technology> findByIds(List<Long> ids);
    Long countAll();
}
