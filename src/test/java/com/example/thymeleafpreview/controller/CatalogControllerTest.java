package com.example.thymeleafpreview.controller;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.config.WebMvcConfig;
import com.example.thymeleafpreview.model.CatalogPage;
import com.example.thymeleafpreview.model.TemplateInfo;
import com.example.thymeleafpreview.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CatalogController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class
    )
)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private PreviewProperties previewProperties;

    // T048: /catalog endpoint tests
    @Test
    void catalog_withoutParams_returnsFirstPage() throws Exception {
        // Given
        List<TemplateInfo> templates = List.of(
            new TemplateInfo("index.html", "index.html", 100, Instant.now()),
            new TemplateInfo("about.html", "about.html", 200, Instant.now())
        );
        CatalogPage catalogPage = new CatalogPage(templates, 0, 20, 2, null);
        when(catalogService.listTemplates(isNull(), eq(0), eq(20)))
            .thenReturn(catalogPage);

        // When/Then
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attributeExists("catalog"))
            .andExpect(model().attribute("catalog", catalogPage));
    }

    @Test
    void catalog_withPageParam_returnsRequestedPage() throws Exception {
        // Given
        List<TemplateInfo> templates = List.of(
            new TemplateInfo("template6.html", "template6.html", 100, Instant.now())
        );
        CatalogPage catalogPage = new CatalogPage(templates, 1, 20, 25, null);
        when(catalogService.listTemplates(isNull(), eq(1), eq(20)))
            .thenReturn(catalogPage);

        // When/Then
        mockMvc.perform(get("/catalog")
                .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attribute("catalog", catalogPage));
    }

    // T049: /catalog with search parameter tests
    @Test
    void catalog_withSearchParam_filtersResults() throws Exception {
        // Given
        List<TemplateInfo> templates = List.of(
            new TemplateInfo("about.html", "about.html", 100, Instant.now())
        );
        CatalogPage catalogPage = new CatalogPage(templates, 0, 20, 1, "about");
        when(catalogService.listTemplates(eq("about"), eq(0), eq(20)))
            .thenReturn(catalogPage);

        // When/Then
        mockMvc.perform(get("/catalog")
                .param("q", "about"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attribute("catalog", catalogPage));
    }

    @Test
    void catalog_withSearchAndPage_passesParameters() throws Exception {
        // Given
        List<TemplateInfo> templates = List.of(
            new TemplateInfo("pages/about.html", "about.html", 100, Instant.now())
        );
        CatalogPage catalogPage = new CatalogPage(templates, 2, 20, 50, "pages");
        when(catalogService.listTemplates(eq("pages"), eq(2), eq(20)))
            .thenReturn(catalogPage);

        // When/Then
        mockMvc.perform(get("/catalog")
                .param("q", "pages")
                .param("page", "2"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attribute("catalog", catalogPage));
    }

    @Test
    void catalog_emptyResult_showsEmptyState() throws Exception {
        // Given
        CatalogPage emptyPage = new CatalogPage(List.of(), 0, 20, 0, "nonexistent");
        when(catalogService.listTemplates(eq("nonexistent"), eq(0), eq(20)))
            .thenReturn(emptyPage);

        // When/Then
        mockMvc.perform(get("/catalog")
                .param("q", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(view().name("catalog"))
            .andExpect(model().attribute("catalog", emptyPage));
    }

    @Test
    void catalog_response_hasNoCacheHeaders() throws Exception {
        // Given
        CatalogPage catalogPage = new CatalogPage(List.of(), 0, 20, 0, null);
        when(catalogService.listTemplates(isNull(), eq(0), eq(20)))
            .thenReturn(catalogPage);

        // When/Then
        mockMvc.perform(get("/catalog"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"));
    }
}
