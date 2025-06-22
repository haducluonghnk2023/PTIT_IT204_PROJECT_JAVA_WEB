package com.data.ra.entity.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Page<T> {
    private List<T> content;
    private long totalItems;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    public Page(List<T> content, long totalItems, int currentPage, int pageSize) {
        this.content = content;
        this.totalItems = totalItems;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

}
