package com.example.thymeleafpreview.exception;

public class TemplateNotFoundException extends RuntimeException {

    private final String templatePath;

    public TemplateNotFoundException(String templatePath) {
        super("Template not found: " + templatePath);
        this.templatePath = templatePath;
    }

    public TemplateNotFoundException(String templatePath, Throwable cause) {
        super("Template not found: " + templatePath, cause);
        this.templatePath = templatePath;
    }

    public String getTemplatePath() {
        return templatePath;
    }
}
