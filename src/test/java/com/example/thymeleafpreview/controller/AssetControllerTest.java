package com.example.thymeleafpreview.controller;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.config.WebMvcConfig;
import org.junit.jupiter.api.BeforeEach;
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
class AssetControllerTest {

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

        // Create CSS directory and file
        Path cssDir = templatesRoot.resolve("css");
        Files.createDirectories(cssDir);
        Files.writeString(cssDir.resolve("style.css"), """
            body {
                background-color: #fff;
                font-family: sans-serif;
            }
            """);

        // Create JS directory and file
        Path jsDir = templatesRoot.resolve("js");
        Files.createDirectories(jsDir);
        Files.writeString(jsDir.resolve("app.js"), """
            console.log('Hello from app.js');
            """);

        // Create images directory and file (PNG header bytes)
        Path imgDir = templatesRoot.resolve("images");
        Files.createDirectories(imgDir);
        // Minimal valid PNG (1x1 transparent pixel)
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82
        };
        Files.write(imgDir.resolve("logo.png"), pngBytes);

        // Create global.json (required by PreviewProperties)
        Files.writeString(defsRoot.resolve("global.json"), "{}");

        registry.add("preview.templates-root", () -> templatesRoot.toString());
        registry.add("preview.defs-root", () -> defsRoot.toString());
    }

    // T036: CSS file serving
    @Test
    void assets_serveCssFile_returnsContentWithCorrectType() throws Exception {
        mockMvc.perform(get("/assets/css/style.css"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/css"))
            .andExpect(content().string(containsString("background-color")));
    }

    // T036: JS file serving
    @Test
    void assets_serveJsFile_returnsContentWithCorrectType() throws Exception {
        mockMvc.perform(get("/assets/js/app.js"))
            .andExpect(status().isOk())
            // JS content type can be application/javascript or text/javascript
            .andExpect(content().string(containsString("console.log")));
    }

    // T037: Image file serving
    @Test
    void assets_serveImageFile_returnsContentWithCorrectType() throws Exception {
        mockMvc.perform(get("/assets/images/logo.png"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("image/png"));
    }

    // T038: Path traversal prevention
    @Test
    void assets_pathTraversal_returns404() throws Exception {
        mockMvc.perform(get("/assets/../../../etc/passwd"))
            .andExpect(status().isNotFound());
    }

    @Test
    void assets_encodedPathTraversal_returns404() throws Exception {
        mockMvc.perform(get("/assets/..%2F..%2F..%2Fetc%2Fpasswd"))
            .andExpect(status().isNotFound());
    }

    @Test
    void assets_nonExistentFile_returns404() throws Exception {
        mockMvc.perform(get("/assets/nonexistent.css"))
            .andExpect(status().isNotFound());
    }

    @Test
    void assets_response_hasNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/assets/css/style.css"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }
}
