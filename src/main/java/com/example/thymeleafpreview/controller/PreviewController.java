package com.example.thymeleafpreview.controller;

import com.example.thymeleafpreview.model.DefinitionData;
import com.example.thymeleafpreview.service.DefinitionService;
import com.example.thymeleafpreview.service.TemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller for template preview functionality.
 */
@Controller
public class PreviewController {

    private final TemplateService templateService;
    private final DefinitionService definitionService;

    public PreviewController(TemplateService templateService, DefinitionService definitionService) {
        this.templateService = templateService;
        this.definitionService = definitionService;
    }

    /**
     * Preview a Thymeleaf template with mock data injection.
     *
     * @param tpl relative path to template (e.g., "pages/index.html")
     * @param fragment optional fragment name to render
     * @param model Spring MVC model
     * @param response HTTP response for setting headers
     * @return view name
     */
    @GetMapping("/preview")
    public String preview(
            @RequestParam String tpl,
            @RequestParam(required = false) String fragment,
            Model model,
            HttpServletResponse response) {

        // Set no-cache headers for hot reload
        response.setHeader(HttpHeaders.CACHE_CONTROL,
            "no-cache, no-store, must-revalidate");

        // Validate and resolve template path
        templateService.resolveSecurely(tpl);

        // Get Thymeleaf template name (without .html suffix)
        String templateName = templateService.getTemplateName(tpl);

        // Load definitions and inject into model
        DefinitionData definitions = definitionService.loadDefinitions(tpl);

        // Add all fixtures to model
        definitions.getFixtures().forEach(model::addAttribute);

        // Add preview-specific attributes (prefixed with _ to avoid collision)
        model.addAttribute("_templatePath", templateName);
        model.addAttribute("_previewCss", definitions.getCss());
        model.addAttribute("_previewJs", definitions.getJs());
        model.addAttribute("_dialects", definitions.getDialects());

        // Add fragment if specified
        if (fragment != null && !fragment.isBlank()) {
            model.addAttribute("_fragment", fragment);
        }

        return "preview-wrapper";
    }
}
