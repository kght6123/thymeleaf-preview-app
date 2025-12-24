package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.exception.InvalidPathException;
import com.example.thymeleafpreview.exception.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateServiceTest {

    @TempDir
    Path tempDir;

    private TemplateService templateService;
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

        templateService = new TemplateService(previewProperties);
    }

    // T015: Path resolution tests
    @Test
    void resolveSecurely_withValidPath_returnsResolvedPath() throws IOException {
        // Given
        Path pagesDir = templatesRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        Path templateFile = pagesDir.resolve("index.html");
        Files.writeString(templateFile, "<html></html>");

        // When
        Path result = templateService.resolveSecurely("pages/index.html");

        // Then
        assertEquals(templateFile, result);
        assertTrue(Files.exists(result));
    }

    @Test
    void resolveSecurely_withNonExistentFile_throwsTemplateNotFoundException() {
        // When/Then
        assertThrows(TemplateNotFoundException.class, () ->
            templateService.resolveSecurely("nonexistent.html")
        );
    }

    @Test
    void resolveSecurely_withRootTemplate_returnsResolvedPath() throws IOException {
        // Given
        Path templateFile = templatesRoot.resolve("index.html");
        Files.writeString(templateFile, "<html></html>");

        // When
        Path result = templateService.resolveSecurely("index.html");

        // Then
        assertEquals(templateFile, result);
    }

    // T016: Path traversal prevention tests
    @Test
    void resolveSecurely_withPathTraversal_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely("../etc/passwd")
        );
    }

    @Test
    void resolveSecurely_withEncodedPathTraversal_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely("..%2F..%2Fetc%2Fpasswd")
        );
    }

    @Test
    void resolveSecurely_withAbsolutePath_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely("/etc/passwd")
        );
    }

    @Test
    void resolveSecurely_withDoubleDotsInMiddle_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely("pages/../../../etc/passwd")
        );
    }

    @Test
    void resolveSecurely_withNullPath_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely(null)
        );
    }

    @Test
    void resolveSecurely_withEmptyPath_throwsInvalidPathException() {
        // When/Then
        assertThrows(InvalidPathException.class, () ->
            templateService.resolveSecurely("")
        );
    }

    @Test
    void getTemplateName_removesHtmlSuffix() throws IOException {
        // Given
        Path templateFile = templatesRoot.resolve("index.html");
        Files.writeString(templateFile, "<html></html>");

        // When
        String result = templateService.getTemplateName("index.html");

        // Then
        assertEquals("index", result);
    }

    @Test
    void getTemplateName_withNestedPath_removesHtmlSuffix() {
        // When
        String result = templateService.getTemplateName("pages/about.html");

        // Then
        assertEquals("pages/about", result);
    }
}
