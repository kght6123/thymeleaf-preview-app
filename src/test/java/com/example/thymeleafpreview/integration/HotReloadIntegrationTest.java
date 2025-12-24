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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for hot reload functionality.
 * Verifies that file changes are reflected on browser refresh without server restart.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HotReloadIntegrationTest {

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

        // Create initial template
        Files.writeString(templatesRoot.resolve("hotreload.html"),
            "<html><body><h1>Original Content</h1></body></html>");

        // Create initial JSON definition
        Files.writeString(defsRoot.resolve("hotreload.json"), """
            {
                "fixtures": {
                    "title": "Original Title"
                }
            }
            """);

        // Create global.json
        Files.writeString(defsRoot.resolve("global.json"), "{}");

        // Create CSS file for asset testing
        Path cssDir = templatesRoot.resolve("css");
        Files.createDirectories(cssDir);
        Files.writeString(cssDir.resolve("style.css"), "body { background: white; }");

        registry.add("preview.templates-root", () -> templatesRoot.toString());
        registry.add("preview.defs-root", () -> defsRoot.toString());
    }

    // T061: Template hot reload test
    @Test
    void hotReload_templateChange_reflectedOnNextRequest() throws Exception {
        // First request - original content
        mockMvc.perform(get("/preview").param("tpl", "hotreload.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Original Content")));

        // Modify template file
        Files.writeString(templatesRoot.resolve("hotreload.html"),
            "<html><body><h1>Updated Content</h1></body></html>");

        // Second request - should see updated content
        mockMvc.perform(get("/preview").param("tpl", "hotreload.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Updated Content")))
            .andExpect(content().string(not(containsString("Original Content"))));
    }

    // T062: JSON definition hot reload test
    @Test
    void hotReload_jsonChange_reflectedOnNextRequest() throws Exception {
        // Create a template that uses the fixture
        Files.writeString(templatesRoot.resolve("fixture-test.html"),
            "<html><body><span th:text=\"${message}\">placeholder</span></body></html>");

        // Create initial definition
        Files.writeString(defsRoot.resolve("fixture-test.json"), """
            {
                "fixtures": {
                    "message": "Hello World"
                }
            }
            """);

        // First request - original message
        mockMvc.perform(get("/preview").param("tpl", "fixture-test.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Hello World")));

        // Modify JSON definition
        Files.writeString(defsRoot.resolve("fixture-test.json"), """
            {
                "fixtures": {
                    "message": "Goodbye World"
                }
            }
            """);

        // Second request - should see updated message
        mockMvc.perform(get("/preview").param("tpl", "fixture-test.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Goodbye World")))
            .andExpect(content().string(not(containsString("Hello World"))));
    }

    // T063: Static asset hot reload test
    @Test
    void hotReload_cssChange_reflectedOnNextRequest() throws Exception {
        Path cssFile = templatesRoot.resolve("css/style.css");

        // First request - original CSS
        mockMvc.perform(get("/assets/css/style.css"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("background: white")));

        // Modify CSS file
        Files.writeString(cssFile, "body { background: black; color: white; }");

        // Second request - should see updated CSS
        mockMvc.perform(get("/assets/css/style.css"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("background: black")))
            .andExpect(content().string(not(containsString("background: white"))));
    }

    @Test
    void hotReload_previewResponse_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/preview").param("tpl", "hotreload.html"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-cache")))
            .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void hotReload_assetResponse_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/assets/css/style.css"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }

    @Test
    void hotReload_catalogResponse_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }

    @Test
    void hotReload_newTemplateAddedDynamically_appearsInCatalog() throws Exception {
        // Get initial catalog count
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk());

        // Add a new template
        Files.writeString(templatesRoot.resolve("dynamic-new.html"),
            "<html><body>Dynamically Added</body></html>");

        // New template should appear in catalog
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("dynamic-new.html")));

        // And should be previewable
        mockMvc.perform(get("/preview").param("tpl", "dynamic-new.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Dynamically Added")));
    }
}
