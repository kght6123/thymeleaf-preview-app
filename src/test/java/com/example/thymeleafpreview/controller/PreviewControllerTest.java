package com.example.thymeleafpreview.controller;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.config.WebMvcConfig;
import com.example.thymeleafpreview.exception.InvalidPathException;
import com.example.thymeleafpreview.exception.TemplateNotFoundException;
import com.example.thymeleafpreview.model.DefinitionData;
import com.example.thymeleafpreview.service.DefinitionService;
import com.example.thymeleafpreview.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = PreviewController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class
    )
)
class PreviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private DefinitionService definitionService;

    @MockBean
    private PreviewProperties previewProperties;

    // T019: Success case
    @Test
    void preview_withValidTemplate_returnsRenderedHtml() throws Exception {
        // Given
        String templatePath = "pages/index.html";
        when(templateService.resolveSecurely(templatePath))
            .thenReturn(Path.of("/templates/pages/index.html"));
        when(templateService.getTemplateName(templatePath))
            .thenReturn("pages/index");

        DefinitionData definitionData = new DefinitionData();
        definitionData.setCss(List.of("/assets/css/main.css"));
        definitionData.setJs(List.of("/assets/js/app.js"));
        definitionData.setFixtures(Map.of("title", "Test Page"));
        when(definitionService.loadDefinitions(templatePath))
            .thenReturn(definitionData);

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath))
            .andExpect(status().isOk())
            .andExpect(view().name("preview-wrapper"))
            .andExpect(model().attributeExists("_templatePath"))
            .andExpect(model().attributeExists("_previewCss"))
            .andExpect(model().attributeExists("_previewJs"))
            .andExpect(model().attribute("title", "Test Page"));
    }

    // T020: 404 error case
    @Test
    void preview_withNonExistentTemplate_returns404() throws Exception {
        // Given
        String templatePath = "nonexistent.html";
        when(templateService.resolveSecurely(templatePath))
            .thenThrow(new TemplateNotFoundException(templatePath));

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error"))
            .andExpect(model().attribute("errorType", "NOT_FOUND"));
    }

    @Test
    void preview_withPathTraversal_returns400() throws Exception {
        // Given
        String templatePath = "../etc/passwd";
        when(templateService.resolveSecurely(templatePath))
            .thenThrow(new InvalidPathException(templatePath));

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("error"))
            .andExpect(model().attribute("errorType", "INVALID_PATH"));
    }

    @Test
    void preview_withoutTplParam_returns400() throws Exception {
        // When/Then
        mockMvc.perform(get("/preview"))
            .andExpect(status().isBadRequest());
    }

    // T021: Fragment parameter
    @Test
    void preview_withFragmentParam_passesFragmentToModel() throws Exception {
        // Given
        String templatePath = "components/header.html";
        String fragment = "navbar";

        when(templateService.resolveSecurely(templatePath))
            .thenReturn(Path.of("/templates/components/header.html"));
        when(templateService.getTemplateName(templatePath))
            .thenReturn("components/header");

        DefinitionData definitionData = new DefinitionData();
        definitionData.setFixtures(Map.of("siteName", "Test"));
        when(definitionService.loadDefinitions(templatePath))
            .thenReturn(definitionData);

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath)
                .param("fragment", fragment))
            .andExpect(status().isOk())
            .andExpect(view().name("preview-wrapper"))
            .andExpect(model().attribute("_fragment", fragment));
    }

    @Test
    void preview_withoutFragmentParam_fragmentIsNull() throws Exception {
        // Given
        String templatePath = "pages/index.html";
        when(templateService.resolveSecurely(templatePath))
            .thenReturn(Path.of("/templates/pages/index.html"));
        when(templateService.getTemplateName(templatePath))
            .thenReturn("pages/index");

        DefinitionData definitionData = new DefinitionData();
        when(definitionService.loadDefinitions(templatePath))
            .thenReturn(definitionData);

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath))
            .andExpect(status().isOk())
            .andExpect(model().attributeDoesNotExist("_fragment"));
    }

    @Test
    void preview_response_hasNoCacheHeaders() throws Exception {
        // Given
        String templatePath = "pages/index.html";
        when(templateService.resolveSecurely(templatePath))
            .thenReturn(Path.of("/templates/pages/index.html"));
        when(templateService.getTemplateName(templatePath))
            .thenReturn("pages/index");
        when(definitionService.loadDefinitions(templatePath))
            .thenReturn(new DefinitionData());

        // When/Then
        mockMvc.perform(get("/preview")
                .param("tpl", templatePath))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"));
    }
}
