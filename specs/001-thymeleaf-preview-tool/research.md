# Research: Thymeleaf Preview Tool

**Date**: 2025-12-24
**Plan**: [plan.md](./plan.md)

## Research Summary

This document captures technical decisions and best practices research for
implementing the Thymeleaf Preview Tool.

---

## 1. FileSystem-based Template Resolution

### Decision
Use `FileTemplateResolver` with file-system paths instead of classpath
resolution to enable hot reload.

### Rationale
- Classpath resources are typically cached by the JVM classloader
- FileTemplateResolver reads files directly from disk on each request
- Setting `setCacheable(false)` ensures templates are never cached
- This aligns with Constitution Principle III (Developer Experience)

### Alternatives Considered
1. **Spring DevTools LiveReload**: Requires additional dependency and browser
   extension. Rejected for simplicity.
2. **Classpath with cache TTL=0**: Still has classloader caching issues.
   Rejected for reliability.
3. **Custom ResourceResolver**: Over-engineered for this use case. Rejected
   for YAGNI.

### Implementation Pattern
```java
@Bean
public ITemplateResolver templateResolver(PreviewProperties props) {
    FileTemplateResolver resolver = new FileTemplateResolver();
    resolver.setPrefix(props.getTemplatesRoot() + "/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCacheable(false);
    resolver.setCheckExistence(true);
    return resolver;
}
```

---

## 2. Wrapper Template Strategy (th:insert)

### Decision
Use a fixed `preview-wrapper.html` template that dynamically inserts the
target template or fragment using `th:insert`.

### Rationale
- Enables consistent preview chrome (optional CSS injection, error display)
- Supports both full template and fragment rendering with same endpoint
- Avoids modifying target templates (Constitution Principle I)
- Thymeleaf's th:insert handles relative path resolution automatically

### Alternatives Considered
1. **Direct template rendering**: No wrapper overhead but loses ability to
   inject preview-specific resources. Rejected for reduced flexibility.
2. **AJAX-based fragment loading**: More complex, requires JavaScript.
   Rejected for simplicity.

### Implementation Pattern
```html
<!-- preview-wrapper.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- Inject CSS from global.json if present -->
    <link th:each="css : ${_previewCss}" th:href="${css}" rel="stylesheet"/>
</head>
<body>
    <!-- Full template mode -->
    <div th:if="${_fragment == null}"
         th:insert="~{__${_templatePath}__}"></div>

    <!-- Fragment mode -->
    <div th:if="${_fragment != null}"
         th:insert="~{__${_templatePath}__ :: __${_fragment}__}"></div>

    <!-- Inject JS from global.json if present -->
    <script th:each="js : ${_previewJs}" th:src="${js}"></script>
</body>
</html>
```

---

## 3. JSON Merging Strategy

### Decision
Use Jackson's `ObjectMapper.updateValue()` for deep merge with template-specific
values taking precedence over global values.

### Rationale
- Jackson is already included in Spring Boot (no extra dependency)
- updateValue() handles nested object merging naturally
- Simple to understand: global.json is base, template.json overlays

### Alternatives Considered
1. **Map.putAll()**: Shallow merge only, loses nested structure. Rejected.
2. **JSON Patch (RFC 6902)**: Overkill for simple override semantics. Rejected.
3. **Custom recursive merge**: Reinventing what Jackson already provides.
   Rejected.

### Implementation Pattern
```java
public Map<String, Object> loadDefinitions(String templatePath) {
    Map<String, Object> global = loadJson(defsRoot + "/global.json");
    Map<String, Object> specific = loadJson(
        defsRoot + "/" + templatePath.replace(".html", ".json")
    );

    // Deep merge: specific overrides global
    ObjectMapper mapper = new ObjectMapper();
    return mapper.updateValue(global, specific);
}
```

---

## 4. Static Resource Mapping

### Decision
Use Spring's `WebMvcConfigurer.addResourceHandlers()` to map `/assets/**`
to `file:${templatesRoot}/`.

### Rationale
- Native Spring mechanism, no custom code needed
- Supports all file types automatically
- Can set cache headers via `CacheControl.noCache()`
- File-based serving aligns with hot reload requirement

### Alternatives Considered
1. **Custom controller with StreamingResponseBody**: More control but
   unnecessary complexity. Rejected.
2. **Symlink assets to classpath**: Violates non-intrusive principle. Rejected.

### Implementation Pattern
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/assets/**")
            .addResourceLocations("file:" + templatesRoot + "/")
            .setCacheControl(CacheControl.noCache());
}
```

---

## 5. Catalog File Enumeration

### Decision
Use `Files.walk()` with lazy Stream processing and filter for `.html` files.

### Rationale
- Lazy evaluation prevents loading all 10,000+ file metadata at once
- Stream can be limited/paginated at the controller level
- Native Java NIO, no external dependency

### Alternatives Considered
1. **Pre-indexed file list with file watcher**: More complex, requires
   background thread. Rejected for MVP.
2. **Apache Commons IO FileUtils**: Additional dependency for no benefit.
   Rejected.

### Implementation Pattern
```java
public Stream<TemplateInfo> listTemplates(String searchTerm) {
    return Files.walk(templatesRoot)
        .filter(p -> p.toString().endsWith(".html"))
        .filter(p -> searchTerm == null ||
                     p.toString().contains(searchTerm))
        .map(p -> new TemplateInfo(
            templatesRoot.relativize(p).toString(),
            Files.getLastModifiedTime(p)
        ));
}
```

---

## 6. Path Traversal Prevention

### Decision
Use Path.normalize() and startsWith() check to prevent directory traversal
attacks.

### Rationale
- Security-critical: prevent access to files outside templatesRoot
- Simple validation pattern that's easy to test
- Reusable for both template and asset paths

### Alternatives Considered
1. **Regex-based path validation**: Error-prone, easy to miss edge cases.
   Rejected.
2. **Web application firewall rules**: External dependency, harder to test.
   Rejected.

### Implementation Pattern
```java
public Path resolveSecurely(String userPath) {
    Path resolved = templatesRoot.resolve(userPath).normalize();
    if (!resolved.startsWith(templatesRoot)) {
        throw new InvalidPathException("Path traversal attempt: " + userPath);
    }
    if (!Files.exists(resolved)) {
        throw new TemplateNotFoundException("Not found: " + userPath);
    }
    return resolved;
}
```

---

## 7. Configuration Properties

### Decision
Use `@ConfigurationProperties` with relaxed binding for command-line args
and environment variables.

### Rationale
- Spring Boot's relaxed binding auto-maps:
  - `--templates-root` → `templatesRoot`
  - `TEMPLATES_ROOT` → `templatesRoot`
- Validation with `@NotNull` ensures required paths are provided
- Clear error messages on startup for missing configuration

### Alternatives Considered
1. **@Value injection**: Less structured, harder to validate as a group.
   Rejected.
2. **Custom command-line parser**: Reinventing Spring Boot capability.
   Rejected.

### Implementation Pattern
```java
@ConfigurationProperties(prefix = "preview")
@Validated
public record PreviewProperties(
    @NotNull Path templatesRoot,
    @NotNull Path defsRoot,
    @Value("${server.port:8080}") int port
) {}
```

---

## 8. Error Handling Strategy

### Decision
Use `@ControllerAdvice` with specific exception handlers returning
descriptive HTML error pages.

### Rationale
- Consistent error presentation across all endpoints
- Includes file paths in error messages for debugging (Constitution Principle IV)
- Returns appropriate HTTP status codes (404, 500)

### Implementation Pattern
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model, TemplateNotFoundException e) {
        model.addAttribute("error", "Template not found");
        model.addAttribute("path", e.getPath());
        return "error";
    }

    @ExceptionHandler(JsonParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleJsonError(Model model, JsonParseException e) {
        model.addAttribute("error", "Invalid JSON");
        model.addAttribute("file", e.getLocation().sourceRef());
        model.addAttribute("line", e.getLocation().getLineNr());
        return "error";
    }
}
```

---

## Open Questions Resolved

All technical clarifications have been addressed in this research document.
No blockers remain for Phase 1 design.
