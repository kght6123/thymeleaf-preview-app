package com.example.thymeleafpreview.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "preview")
@Validated
public class PreviewProperties {

    private static final Logger log = LoggerFactory.getLogger(PreviewProperties.class);

    @NotNull(message = "templates-root must be specified")
    private Path templatesRoot;

    @NotNull(message = "defs-root must be specified")
    private Path defsRoot;

    public Path getTemplatesRoot() {
        return templatesRoot;
    }

    public void setTemplatesRoot(Path templatesRoot) {
        this.templatesRoot = templatesRoot;
    }

    public Path getDefsRoot() {
        return defsRoot;
    }

    public void setDefsRoot(Path defsRoot) {
        this.defsRoot = defsRoot;
    }

    @PostConstruct
    public void validate() {
        // Skip validation if properties are null (test mode with mocks)
        if (templatesRoot == null || defsRoot == null) {
            log.debug("Skipping validation - properties not fully configured");
            return;
        }

        log.info("Validating preview configuration...");

        if (templatesRoot.toString().isBlank()) {
            throw new IllegalStateException(
                "templates-root must be specified. " +
                "Use --preview.templates-root=<path> or set TEMPLATES_ROOT environment variable."
            );
        }

        if (defsRoot.toString().isBlank()) {
            throw new IllegalStateException(
                "defs-root must be specified. " +
                "Use --preview.defs-root=<path> or set DEFS_ROOT environment variable."
            );
        }

        if (!Files.exists(templatesRoot)) {
            throw new IllegalStateException(
                "templates-root directory does not exist: " + templatesRoot.toAbsolutePath()
            );
        }

        if (!Files.isReadable(templatesRoot)) {
            throw new IllegalStateException(
                "templates-root directory is not readable: " + templatesRoot.toAbsolutePath()
            );
        }

        if (!Files.exists(defsRoot)) {
            throw new IllegalStateException(
                "defs-root directory does not exist: " + defsRoot.toAbsolutePath()
            );
        }

        if (!Files.isReadable(defsRoot)) {
            throw new IllegalStateException(
                "defs-root directory is not readable: " + defsRoot.toAbsolutePath()
            );
        }

        log.info("Preview configuration validated successfully");
        log.info("  Templates root: {}", templatesRoot.toAbsolutePath());
        log.info("  Definitions root: {}", defsRoot.toAbsolutePath());
    }
}
