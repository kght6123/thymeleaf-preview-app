package com.example.thymeleafpreview.model;

import java.time.Instant;

/**
 * Information about a template file for the catalog.
 */
public class TemplateInfo {

    private final String path;
    private final String name;
    private final long size;
    private final Instant lastModified;

    public TemplateInfo(String path, String name, long size, Instant lastModified) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public Instant getLastModified() {
        return lastModified;
    }
}
