package com.example.thymeleafpreview;

import com.example.thymeleafpreview.config.PreviewProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ThymeleafPreviewApplication {

    private static final Logger log = LoggerFactory.getLogger(ThymeleafPreviewApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ThymeleafPreviewApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "preview.templates-root")
    CommandLineRunner startupBanner(PreviewProperties properties, Environment env) {
        return args -> {
            // Skip banner if properties are not fully configured (e.g., in tests with mocks)
            if (properties.getTemplatesRoot() == null || properties.getDefsRoot() == null) {
                return;
            }

            String port = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");

            log.info("");
            log.info("=".repeat(60));
            log.info("  Thymeleaf Preview Tool Started");
            log.info("=".repeat(60));
            log.info("");
            log.info("  Configuration:");
            log.info("    Templates: {}", properties.getTemplatesRoot().toAbsolutePath());
            log.info("    Definitions: {}", properties.getDefsRoot().toAbsolutePath());
            log.info("");
            log.info("  Endpoints:");
            log.info("    Catalog:  http://localhost:{}{}/catalog", port, contextPath);
            log.info("    Preview:  http://localhost:{}{}/preview?tpl=<template.html>", port, contextPath);
            log.info("    Assets:   http://localhost:{}{}/assets/<path>", port, contextPath);
            log.info("");
            log.info("  Hot Reload: Enabled (no server restart needed)");
            log.info("=".repeat(60));
            log.info("");
        };
    }
}
