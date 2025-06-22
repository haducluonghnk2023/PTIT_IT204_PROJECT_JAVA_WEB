package com.data.ra.repository.admin;

import com.data.ra.entity.admin.Technology;

import java.util.List;

public interface TechnologyRepository {
    List<Technology> findAll(int page, int size, String search);
    List<Technology> findAlls();
    Technology findById(Long id);
    void save(Technology technology);
    void deleteById(Long id);
    long count(String search);
    Technology findByName(String name);
    List<Technology> findByIds(List<Long> ids);
    Long countAll();
}
