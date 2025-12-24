package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.model.CatalogPage;
import com.example.thymeleafpreview.model.TemplateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for browsing and searching template files.
 */
@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final PreviewProperties previewProperties;

    public CatalogService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    /**
     * Lists templates with optional search filtering and pagination.
     *
     * @param searchQuery optional search query to filter templates (case-insensitive)
     * @param page page number (0-indexed)
     * @param pageSize number of templates per page
     * @return paginated catalog result
     */
    public CatalogPage listTemplates(String searchQuery, int page, int pageSize) {
        Path templatesRoot = previewProperties.getTemplatesRoot();

        try (Stream<Path> paths = Files.walk(templatesRoot)) {
            List<TemplateInfo> allTemplates = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".html"))
                .map(p -> createTemplateInfo(templatesRoot, p))
                .filter(info -> matchesSearch(info, searchQuery))
                .sorted(Comparator.comparing(TemplateInfo::getPath))
                .toList();

            long totalTemplates = allTemplates.size();

            List<TemplateInfo> pageTemplates = allTemplates.stream()
                .skip((long) page * pageSize)
                .limit(pageSize)
                .toList();

            return new CatalogPage(pageTemplates, page, pageSize, totalTemplates, searchQuery);
        } catch (IOException e) {
            log.error("Failed to enumerate templates: {}", e.getMessage(), e);
            return new CatalogPage(List.of(), page, pageSize, 0, searchQuery);
        }
    }

    private TemplateInfo createTemplateInfo(Path templatesRoot, Path templatePath) {
        String relativePath = templatesRoot.relativize(templatePath).toString().replace("\\", "/");
        String name = templatePath.getFileName().toString();

        try {
            BasicFileAttributes attrs = Files.readAttributes(templatePath, BasicFileAttributes.class);
            return new TemplateInfo(relativePath, name, attrs.size(), attrs.lastModifiedTime().toInstant());
        } catch (IOException e) {
            log.warn("Failed to read attributes for {}: {}", templatePath, e.getMessage());
            return new TemplateInfo(relativePath, name, 0, null);
        }
    }

    private boolean matchesSearch(TemplateInfo info, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }
        String lowerQuery = searchQuery.toLowerCase();
        return info.getPath().toLowerCase().contains(lowerQuery);
    }
}
