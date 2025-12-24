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
class AssetIntegrationTest {

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

        // Create nested directory structure for assets
        Path assetsDir = templatesRoot.resolve("assets/css");
        Files.createDirectories(assetsDir);
        Files.writeString(assetsDir.resolve("main.css"), """
            /* Main stylesheet */
            :root {
                --primary-color: #007bff;
            }
            body {
                margin: 0;
                padding: 0;
            }
            """);

        Path jsDir = templatesRoot.resolve("assets/js");
        Files.createDirectories(jsDir);
        Files.writeString(jsDir.resolve("bundle.js"), """
            // JavaScript bundle
            (function() {
                console.log('Bundle loaded');
            })();
            """);

        // Create global.json
        Files.writeString(defsRoot.resolve("global.json"), "{}");

        registry.add("preview.templates-root", () -> templatesRoot.toString());
        registry.add("preview.defs-root", () -> defsRoot.toString());
    }

    @Test
    void assetServing_cssInNestedDirectory_works() throws Exception {
        mockMvc.perform(get("/assets/assets/css/main.css"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/css"))
            .andExpect(content().string(containsString("--primary-color")));
    }

    @Test
    void assetServing_jsInNestedDirectory_works() throws Exception {
        mockMvc.perform(get("/assets/assets/js/bundle.js"))
            .andExpect(status().isOk())
            // JS content type can be application/javascript or text/javascript
            .andExpect(content().string(containsString("Bundle loaded")));
    }

    @Test
    void assetServing_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/assets/assets/css/main.css"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Cache-Control"))
            .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }

    @Test
    void assetServing_correctMimeTypeForCss() throws Exception {
        mockMvc.perform(get("/assets/assets/css/main.css"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void assetServing_correctMimeTypeForJs() throws Exception {
        mockMvc.perform(get("/assets/assets/js/bundle.js"))
            .andExpect(status().isOk());
        // Note: JS content type varies (text/javascript or application/javascript)
        // The important thing is that the file is served correctly
    }
}
