package com.example.thymeleafpreview.controller;

import com.example.thymeleafpreview.model.CatalogPage;
import com.example.thymeleafpreview.service.CatalogService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller for the template catalog/browser UI.
 */
@Controller
public class CatalogController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(name = "q", required = false) String searchQuery,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model,
            HttpServletResponse response) {

        // Set no-cache headers
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        CatalogPage catalogPage = catalogService.listTemplates(searchQuery, page, DEFAULT_PAGE_SIZE);
        model.addAttribute("catalog", catalogPage);

        return "catalog";
    }
}
