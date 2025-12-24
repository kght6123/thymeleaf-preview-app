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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogIntegrationTest {

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

        // Create a large number of templates for pagination testing
        for (int i = 1; i <= 50; i++) {
            String fileName = "template" + String.format("%02d", i) + ".html";
            Files.writeString(templatesRoot.resolve(fileName), "<html><body>Template " + i + "</body></html>");
        }

        // Create some nested templates
        Path pagesDir = templatesRoot.resolve("pages");
        Files.createDirectories(pagesDir);
        Files.writeString(pagesDir.resolve("home.html"), "<html><body>Home</body></html>");
        Files.writeString(pagesDir.resolve("about.html"), "<html><body>About</body></html>");

        Path componentsDir = templatesRoot.resolve("components");
        Files.createDirectories(componentsDir);
        Files.writeString(componentsDir.resolve("header.html"), "<html><body>Header</body></html>");
        Files.writeString(componentsDir.resolve("footer.html"), "<html><body>Footer</body></html>");

        // Create global.json
        Files.writeString(defsRoot.resolve("global.json"), "{}");

        registry.add("preview.templates-root", () -> templatesRoot.toString());
        registry.add("preview.defs-root", () -> defsRoot.toString());
    }

    // T050: Integration test for catalog with large file count
    @Test
    void catalog_withLargeFileCount_paginatesCorrectly() throws Exception {
        // Total: 50 numbered + 2 pages + 2 components = 54 templates
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attributeExists("catalog"))
            .andExpect(xpath("//div[@class='template-list']").exists());
    }

    @Test
    void catalog_secondPage_showsCorrectTemplates() throws Exception {
        mockMvc.perform(get("/catalog")
                .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"));
    }

    @Test
    void catalog_searchForPages_returnsOnlyPagesDirectory() throws Exception {
        mockMvc.perform(get("/catalog")
                .param("q", "pages"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"));
    }

    @Test
    void catalog_searchForComponents_returnsOnlyComponentsDirectory() throws Exception {
        mockMvc.perform(get("/catalog")
                .param("q", "components"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"));
    }

    @Test
    void catalog_templateLink_pointsToPreviewEndpoint() throws Exception {
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/preview?tpl=")));
    }

    @Test
    void catalog_showsSearchForm() throws Exception {
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"q\"")));
    }

    @Test
    void catalog_response_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Cache-Control"))
            .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }
}
