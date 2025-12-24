package com.example.thymeleafpreview.exception;

import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TemplateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTemplateNotFound(TemplateNotFoundException e, Model model) {
        log.warn("Template not found: {}", e.getTemplatePath());
        model.addAttribute("errorType", "NOT_FOUND");
        model.addAttribute("errorMessage", "Template not found");
        model.addAttribute("path", e.getTemplatePath());
        model.addAttribute("suggestion", "Check that the template path is correct and the file exists in the templates directory.");
        return "error";
    }

    @ExceptionHandler(InvalidPathException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidPath(InvalidPathException e, Model model) {
        log.warn("Invalid path attempted: {}", e.getAttemptedPath());
        model.addAttribute("errorType", "INVALID_PATH");
        model.addAttribute("errorMessage", "Invalid path");
        model.addAttribute("path", e.getAttemptedPath());
        model.addAttribute("suggestion", "Path contains invalid characters or traversal sequences (..). Use a valid relative path.");
        return "error";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingParameter(MissingServletRequestParameterException e, Model model) {
        log.warn("Missing required parameter: {}", e.getParameterName());
        model.addAttribute("errorType", "MISSING_PARAMETER");
        model.addAttribute("errorMessage", "Missing required parameter: " + e.getParameterName());
        model.addAttribute("path", "");
        model.addAttribute("suggestion", "Include the '" + e.getParameterName() + "' query parameter in your request.");
        return "error";
    }

    @ExceptionHandler(JsonParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleJsonParseError(JsonParseException e, Model model) {
        log.error("JSON parse error: {}", e.getMessage());
        String location = e.getLocation() != null ? String.valueOf(e.getLocation().getSourceRef()) : "unknown";
        int lineNumber = e.getLocation() != null ? e.getLocation().getLineNr() : -1;

        model.addAttribute("errorType", "INVALID_JSON");
        model.addAttribute("errorMessage", "Invalid JSON syntax");
        model.addAttribute("path", location);
        model.addAttribute("line", lineNumber);
        model.addAttribute("suggestion", "Check the JSON file for syntax errors. Validate with a JSON linter.");
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException e, Model model) {
        log.warn("Resource not found: {}", e.getResourcePath());
        model.addAttribute("errorType", "NOT_FOUND");
        model.addAttribute("errorMessage", "Resource not found");
        model.addAttribute("path", e.getResourcePath());
        model.addAttribute("suggestion", "Check that the file path is correct and the file exists.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception e, Model model) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        model.addAttribute("errorType", "RENDER_ERROR");
        model.addAttribute("errorMessage", "An unexpected error occurred");
        model.addAttribute("path", "");
        model.addAttribute("suggestion", "Check the application logs for more details.");
        return "error";
    }
}
