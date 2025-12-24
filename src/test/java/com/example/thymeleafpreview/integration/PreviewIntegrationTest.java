package com.example.thymeleafpreview.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PreviewIntegrationTest {

    private static Path templatesRoot;
    private static Path defsRoot;

    @TempDir
    static Path tempDir;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        templatesRoot = tempDir.resolve("templates");
        Files.createDirectories(templatesRoot);

        defsRoot = tempDir.resolve("defs");
        Files.createDirectories(defsRoot);

        // Create test template
        Path pagesDir = templatesRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        Files.writeString(pagesDir.resolve("index.html"), """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org">
            <head>
                <title th:text="${title}">Default Title</title>
            </head>
            <body>
                <h1 th:text="${heading}">Welcome</h1>
                <p>Site: <span th:text="${siteName}">Site Name</span></p>
            </body>
            </html>
            """);

        // Create component with fragment
        Path componentsDir = templatesRoot.resolve("components");
        Files.createDirectories(componentsDir);
        Files.writeString(componentsDir.resolve("header.html"), """
            <!DOCTYPE html>
            <html xmlns:th="http://www.thymeleaf.org">
            <body>
                <nav th:fragment="navbar">
                    <a th:text="${siteName}">Site</a>
                </nav>
            </body>
            </html>
            """);

        // Create global.json
        Files.writeString(defsRoot.resolve("global.json"), """
            {
              "css": ["/assets/css/main.css"],
              "js": ["/assets/js/app.js"],
              "fixtures": {
                "siteName": "Integration Test Site",
                "currentYear": 2025
              }
            }
            """);

        // Create page-specific JSON
        Path defsPages = defsRoot.resolve("pages");
        Files.createDirectories(defsPages);
        Files.writeString(defsPages.resolve("index.json"), """
            {
              "fixtures": {
                "title": "Index Page Title",
                "heading": "Welcome to Integration Test"
              }
            }
            """);

        registry.add("preview.templates-root", () -> templatesRoot.toString());
        registry.add("preview.defs-root", () -> defsRoot.toString());
    }

    @Test
    void previewFullTemplate_rendersWithMergedData() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "pages/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Index Page Title")))
            .andExpect(content().string(containsString("Welcome to Integration Test")))
            .andExpect(content().string(containsString("Integration Test Site")));
    }

    @Test
    void previewFragment_rendersOnlyFragment() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "components/header.html")
                .param("fragment", "navbar"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Integration Test Site")));
    }

    @Test
    void previewNonExistent_returns404WithErrorPage() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "nonexistent.html"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Template not found")));
    }

    @Test
    void previewPathTraversal_returns400() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "../../../etc/passwd"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Invalid path")));
    }

    @Test
    void previewResponse_containsCssAndJsFromDefinitions() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "pages/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/assets/css/main.css")))
            .andExpect(content().string(containsString("/assets/js/app.js")));
    }

    @Test
    void previewResponse_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/preview")
                .param("tpl", "pages/index.html"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"));
    }
}
