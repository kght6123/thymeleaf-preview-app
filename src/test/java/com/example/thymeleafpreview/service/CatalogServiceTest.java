package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.model.CatalogPage;
import com.example.thymeleafpreview.model.TemplateInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceTest {

    @TempDir
    Path tempDir;

    private CatalogService catalogService;
    private PreviewProperties previewProperties;
    private Path templatesRoot;

    @BeforeEach
    void setUp() throws IOException {
        templatesRoot = tempDir.resolve("templates");
        Files.createDirectories(templatesRoot);

        Path defsRoot = tempDir.resolve("defs");
        Files.createDirectories(defsRoot);

        previewProperties = mock(PreviewProperties.class);
        when(previewProperties.getTemplatesRoot()).thenReturn(templatesRoot);
        when(previewProperties.getDefsRoot()).thenReturn(defsRoot);

        catalogService = new CatalogService(previewProperties);
    }

    // T045: File enumeration tests
    @Test
    void listTemplates_withMultipleFiles_returnsAllHtmlFiles() throws IOException {
        // Given
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("about.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("style.css"), "/* css */");

        // When
        CatalogPage result = catalogService.listTemplates(null, 0, 10);

        // Then
        assertEquals(2, result.getTotalTemplates());
        List<String> paths = result.getTemplates().stream()
            .map(TemplateInfo::getPath)
            .toList();
        assertTrue(paths.contains("index.html"));
        assertTrue(paths.contains("about.html"));
        assertFalse(paths.contains("style.css"));
    }

    @Test
    void listTemplates_withNestedDirectories_returnsAllHtmlFiles() throws IOException {
        // Given
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");
        Path pagesDir = templatesRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        Files.writeString(pagesDir.resolve("home.html"), "<html></html>");
        Path componentsDir = templatesRoot.resolve("components");
        Files.createDirectories(componentsDir);
        Files.writeString(componentsDir.resolve("header.html"), "<html></html>");

        // When
        CatalogPage result = catalogService.listTemplates(null, 0, 10);

        // Then
        assertEquals(3, result.getTotalTemplates());
        List<String> paths = result.getTemplates().stream()
            .map(TemplateInfo::getPath)
            .toList();
        assertTrue(paths.contains("index.html"));
        assertTrue(paths.contains("pages/home.html"));
        assertTrue(paths.contains("components/header.html"));
    }

    @Test
    void listTemplates_withEmptyDirectory_returnsEmptyList() {
        // When
        CatalogPage result = catalogService.listTemplates(null, 0, 10);

        // Then
        assertEquals(0, result.getTotalTemplates());
        assertTrue(result.getTemplates().isEmpty());
    }

    // T046: Search filtering tests
    @Test
    void listTemplates_withSearchQuery_filtersResults() throws IOException {
        // Given
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("about.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("contact.html"), "<html></html>");

        // When
        CatalogPage result = catalogService.listTemplates("about", 0, 10);

        // Then
        assertEquals(1, result.getTotalTemplates());
        assertEquals("about.html", result.getTemplates().get(0).getPath());
        assertEquals("about", result.getSearchQuery());
    }

    @Test
    void listTemplates_withSearchQuery_matchesPartialPath() throws IOException {
        // Given
        Path pagesDir = templatesRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        Files.writeString(pagesDir.resolve("home.html"), "<html></html>");
        Files.writeString(pagesDir.resolve("about.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");

        // When
        CatalogPage result = catalogService.listTemplates("pages", 0, 10);

        // Then
        assertEquals(2, result.getTotalTemplates());
    }

    @Test
    void listTemplates_withSearchQuery_caseInsensitive() throws IOException {
        // Given
        Files.writeString(templatesRoot.resolve("AboutPage.html"), "<html></html>");
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");

        // When
        CatalogPage result = catalogService.listTemplates("about", 0, 10);

        // Then
        assertEquals(1, result.getTotalTemplates());
        assertEquals("AboutPage.html", result.getTemplates().get(0).getPath());
    }

    @Test
    void listTemplates_withNoMatch_returnsEmptyList() throws IOException {
        // Given
        Files.writeString(templatesRoot.resolve("index.html"), "<html></html>");

        // When
        CatalogPage result = catalogService.listTemplates("nonexistent", 0, 10);

        // Then
        assertEquals(0, result.getTotalTemplates());
        assertTrue(result.getTemplates().isEmpty());
    }

    // T047: Pagination tests
    @Test
    void listTemplates_withPagination_returnsCorrectPage() throws IOException {
        // Given - create 15 files
        for (int i = 1; i <= 15; i++) {
            Files.writeString(templatesRoot.resolve("template" + String.format("%02d", i) + ".html"), "<html></html>");
        }

        // When - get page 0 with size 5
        CatalogPage page0 = catalogService.listTemplates(null, 0, 5);

        // Then
        assertEquals(15, page0.getTotalTemplates());
        assertEquals(5, page0.getTemplates().size());
        assertEquals(0, page0.getPage());
        assertEquals(5, page0.getPageSize());
        assertEquals(3, page0.getTotalPages());
        assertTrue(page0.hasNext());
        assertFalse(page0.hasPrevious());
    }

    @Test
    void listTemplates_withSecondPage_returnsCorrectItems() throws IOException {
        // Given - create 15 files
        for (int i = 1; i <= 15; i++) {
            Files.writeString(templatesRoot.resolve("template" + String.format("%02d", i) + ".html"), "<html></html>");
        }

        // When - get page 1 with size 5
        CatalogPage page1 = catalogService.listTemplates(null, 1, 5);

        // Then
        assertEquals(15, page1.getTotalTemplates());
        assertEquals(5, page1.getTemplates().size());
        assertEquals(1, page1.getPage());
        assertTrue(page1.hasNext());
        assertTrue(page1.hasPrevious());
    }

    @Test
    void listTemplates_withLastPage_hasNoNext() throws IOException {
        // Given - create 15 files
        for (int i = 1; i <= 15; i++) {
            Files.writeString(templatesRoot.resolve("template" + String.format("%02d", i) + ".html"), "<html></html>");
        }

        // When - get last page
        CatalogPage lastPage = catalogService.listTemplates(null, 2, 5);

        // Then
        assertEquals(5, lastPage.getTemplates().size());
        assertFalse(lastPage.hasNext());
        assertTrue(lastPage.hasPrevious());
    }

    @Test
    void listTemplates_templateInfo_containsCorrectData() throws IOException {
        // Given
        Path templateFile = templatesRoot.resolve("index.html");
        String content = "<html><body>Hello</body></html>";
        Files.writeString(templateFile, content);

        // When
        CatalogPage result = catalogService.listTemplates(null, 0, 10);

        // Then
        assertEquals(1, result.getTemplates().size());
        TemplateInfo info = result.getTemplates().get(0);
        assertEquals("index.html", info.getPath());
        assertEquals("index.html", info.getName());
        assertEquals(content.length(), info.getSize());
        assertNotNull(info.getLastModified());
    }
}
