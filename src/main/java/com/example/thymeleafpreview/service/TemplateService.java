package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.exception.InvalidPathException;
import com.example.thymeleafpreview.exception.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for secure template path resolution.
 */
@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final PreviewProperties previewProperties;

    public TemplateService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    /**
     * Resolves a user-provided template path securely.
     * Prevents path traversal attacks and validates file existence.
     *
     * @param userPath relative path to template (e.g., "pages/index.html")
     * @return resolved absolute Path
     * @throws InvalidPathException if path contains traversal sequences or is invalid
     * @throws TemplateNotFoundException if template file does not exist
     */
    public Path resolveSecurely(String userPath) {
        log.debug("Resolving template path: {}", userPath);

        if (userPath == null || userPath.isBlank()) {
            log.debug("Template path is empty or null");
            throw new InvalidPathException("Template path cannot be empty");
        }

        // Decode URL-encoded path components
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(userPath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            decodedPath = userPath;
        }

        // Check for path traversal in decoded path
        if (decodedPath.contains("..")) {
            log.debug("Path traversal detected in: {}", decodedPath);
            throw new InvalidPathException("Invalid path: path traversal not allowed");
        }

        // Reject absolute paths
        if (decodedPath.startsWith("/") || decodedPath.startsWith("\\")) {
            log.debug("Absolute path rejected: {}", decodedPath);
            throw new InvalidPathException("Invalid path: absolute paths not allowed");
        }

        Path templatesRoot = previewProperties.getTemplatesRoot();
        Path resolved = templatesRoot.resolve(decodedPath).normalize();

        // Final security check: ensure resolved path is under templatesRoot
        if (!resolved.startsWith(templatesRoot)) {
            log.debug("Resolved path {} is outside templates root {}", resolved, templatesRoot);
            throw new InvalidPathException("Invalid path: path traversal not allowed");
        }

        // Check file existence
        if (!Files.exists(resolved)) {
            log.debug("Template file not found: {}", resolved);
            throw new TemplateNotFoundException(userPath);
        }

        log.debug("Template resolved successfully: {}", resolved);
        return resolved;
    }

    /**
     * Gets the Thymeleaf template name (without .html suffix).
     *
     * @param templatePath relative path to template
     * @return template name for Thymeleaf
     */
    public String getTemplateName(String templatePath) {
        if (templatePath == null) {
            return null;
        }
        if (templatePath.endsWith(".html")) {
            return templatePath.substring(0, templatePath.length() - 5);
        }
        return templatePath;
    }
}
