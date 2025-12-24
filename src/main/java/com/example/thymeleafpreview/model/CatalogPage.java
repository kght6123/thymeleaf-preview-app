package com.example.thymeleafpreview.model;

import java.util.List;

/**
 * Paginated catalog result containing template list and pagination info.
 */
public class CatalogPage {

    private final List<TemplateInfo> templates;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final long totalTemplates;
    private final String searchQuery;

    public CatalogPage(List<TemplateInfo> templates, int page, int pageSize, long totalTemplates, String searchQuery) {
        this.templates = templates;
        this.page = page;
        this.pageSize = pageSize;
        this.totalTemplates = totalTemplates;
        this.totalPages = (int) Math.ceil((double) totalTemplates / pageSize);
        this.searchQuery = searchQuery;
    }

    public List<TemplateInfo> getTemplates() {
        return templates;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalTemplates() {
        return totalTemplates;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page < totalPages - 1;
    }
}
