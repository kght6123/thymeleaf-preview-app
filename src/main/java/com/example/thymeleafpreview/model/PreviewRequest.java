package com.example.thymeleafpreview.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request parameters for template preview.
 */
public class PreviewRequest {

    @NotBlank(message = "Template path is required")
    @Pattern(regexp = ".*\\.html$", message = "Template must end with .html")
    private String tpl;

    private String fragment;

    public PreviewRequest() {
    }

    public PreviewRequest(String tpl, String fragment) {
        this.tpl = tpl;
        this.fragment = fragment;
    }

    public String getTpl() {
        return tpl;
    }

    public void setTpl(String tpl) {
        this.tpl = tpl;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
}
