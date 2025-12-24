package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.model.DefinitionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefinitionServiceTest {

    @TempDir
    Path tempDir;

    private DefinitionService definitionService;
    private PreviewProperties previewProperties;
    private Path defsRoot;

    @BeforeEach
    void setUp() throws IOException {
        Path templatesRoot = tempDir.resolve("templates");
        Files.createDirectories(templatesRoot);

        defsRoot = tempDir.resolve("defs");
        Files.createDirectories(defsRoot);

        previewProperties = mock(PreviewProperties.class);
        when(previewProperties.getTemplatesRoot()).thenReturn(templatesRoot);
        when(previewProperties.getDefsRoot()).thenReturn(defsRoot);

        definitionService = new DefinitionService(previewProperties);
    }

    // T017: JSON loading tests
    @Test
    void loadDefinitions_withGlobalJsonOnly_returnsGlobalData() throws IOException {
        // Given
        String globalJson = """
            {
              "css": ["/assets/css/main.css"],
              "js": ["/assets/js/app.js"],
              "fixtures": {
                "siteName": "Test Site"
              }
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), globalJson);

        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        assertNotNull(result);
        assertEquals(List.of("/assets/css/main.css"), result.getCss());
        assertEquals(List.of("/assets/js/app.js"), result.getJs());
        assertEquals("Test Site", result.getFixtures().get("siteName"));
    }

    @Test
    void loadDefinitions_withNoJsonFiles_returnsEmptyDefinitionData() {
        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        assertNotNull(result);
        assertTrue(result.getCss().isEmpty());
        assertTrue(result.getJs().isEmpty());
        assertTrue(result.getFixtures().isEmpty());
    }

    @Test
    void loadDefinitions_withTemplateSpecificJsonOnly_returnsSpecificData() throws IOException {
        // Given
        Path pagesDir = defsRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        String specificJson = """
            {
              "fixtures": {
                "title": "Index Page"
              }
            }
            """;
        Files.writeString(pagesDir.resolve("index.json"), specificJson);

        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        assertNotNull(result);
        assertEquals("Index Page", result.getFixtures().get("title"));
    }

    // T018: JSON merging tests
    @Test
    void loadDefinitions_mergesGlobalAndSpecificData() throws IOException {
        // Given
        String globalJson = """
            {
              "css": ["/assets/css/global.css"],
              "fixtures": {
                "siteName": "Global Site",
                "year": 2025
              }
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), globalJson);

        Path pagesDir = defsRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        String specificJson = """
            {
              "fixtures": {
                "title": "Specific Title",
                "siteName": "Overridden Site"
              }
            }
            """;
        Files.writeString(pagesDir.resolve("index.json"), specificJson);

        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        assertNotNull(result);
        // CSS from global
        assertEquals(List.of("/assets/css/global.css"), result.getCss());
        // Fixtures merged with specific overriding global
        assertEquals("Overridden Site", result.getFixtures().get("siteName"));
        assertEquals("Specific Title", result.getFixtures().get("title"));
        assertEquals(2025, result.getFixtures().get("year"));
    }

    @Test
    void loadDefinitions_specificCssReplacesGlobalCss() throws IOException {
        // Given
        String globalJson = """
            {
              "css": ["/assets/css/global.css"]
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), globalJson);

        Path pagesDir = defsRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        String specificJson = """
            {
              "css": ["/assets/css/specific.css", "/assets/css/page.css"]
            }
            """;
        Files.writeString(pagesDir.resolve("index.json"), specificJson);

        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        // Specific CSS completely replaces global CSS (not appended)
        assertEquals(List.of("/assets/css/specific.css", "/assets/css/page.css"), result.getCss());
    }

    @Test
    void loadDefinitions_handlesNestedFixtures() throws IOException {
        // Given
        String globalJson = """
            {
              "fixtures": {
                "user": {
                  "name": "John",
                  "role": "admin"
                }
              }
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), globalJson);

        Path pagesDir = defsRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        String specificJson = """
            {
              "fixtures": {
                "user": {
                  "name": "Jane"
                }
              }
            }
            """;
        Files.writeString(pagesDir.resolve("index.json"), specificJson);

        // When
        DefinitionData result = definitionService.loadDefinitions("pages/index.html");

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) result.getFixtures().get("user");
        assertEquals("Jane", user.get("name"));
        // Note: Deep merge should preserve role from global
        assertEquals("admin", user.get("role"));
    }

    @Test
    void loadDefinitions_readsJsonFreshEachTime() throws IOException {
        // Given
        String globalJson = """
            {
              "fixtures": {
                "counter": 1
              }
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), globalJson);

        // First read
        DefinitionData result1 = definitionService.loadDefinitions("pages/index.html");
        assertEquals(1, result1.getFixtures().get("counter"));

        // Update file
        String updatedJson = """
            {
              "fixtures": {
                "counter": 2
              }
            }
            """;
        Files.writeString(defsRoot.resolve("global.json"), updatedJson);

        // Second read - should see updated value (no caching)
        DefinitionData result2 = definitionService.loadDefinitions("pages/index.html");
        assertEquals(2, result2.getFixtures().get("counter"));
    }
}
