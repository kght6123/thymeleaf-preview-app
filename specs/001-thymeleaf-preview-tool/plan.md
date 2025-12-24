# Implementation Plan: Thymeleaf Preview Tool

**Branch**: `001-thymeleaf-preview-tool` | **Date**: 2025-12-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-thymeleaf-preview-tool/spec.md`

## Summary

Build a local Thymeleaf template preview tool as a Spring Boot application.
Users specify `templatesRoot` (read-only template directory) and `defsRoot`
(JSON definitions directory) at startup. The tool provides:

1. `/preview` endpoint for rendering templates with mock data injection
2. `/catalog` endpoint for browsing and searching templates
3. `/assets/**` for serving static files from templatesRoot
4. Hot reload via no-cache strategy (browser refresh reflects changes)

**Technical Approach**: Use FileSystemResource for template resolution (not
classpath) to enable hot reload. Wrap target templates in a preview-wrapper.html
using th:insert for dynamic embedding. Merge global.json and template-specific
JSON using Jackson for data injection.

## Technical Context

**Language/Version**: Java 21+
**Primary Dependencies**: Spring Boot 3.x (latest), Thymeleaf 3.x, Jackson
**Build Tool**: Maven
**Storage**: File system only (no database)
**Testing**: JUnit 5, Spring Boot Test, MockMvc
**Target Platform**: JVM (Fat JAR), Docker
**Project Type**: Single Spring Boot application
**Performance Goals**:
- Template preview render: < 2 seconds for 100KB templates
- Catalog load: < 3 seconds for 10,000 templates
**Constraints**:
- templatesRoot is read-only (Constitution Principle I)
- No caching for hot reload support
**Scale/Scope**: Support directories with 10,000+ template files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Non-intrusive | No writes to templatesRoot | ✅ PASS |
| II. Performance | Lazy/paginated catalog for 10k+ files | ✅ PASS |
| III. Developer Experience | Hot reload via no-cache headers | ✅ PASS |
| IV. OSS Quality | CLI args/env vars only, 3-min startup | ✅ PASS |

**Gate Verification Details**:

1. **Non-intrusive**: All operations on templatesRoot are read-only.
   Static assets served via Spring's resource handler (read). Templates
   read via FileSystemResource. Definitions stored separately in defsRoot.

2. **Performance**: Catalog uses Files.walk() with lazy streaming. Client-side
   search filtering. Pagination available for very large directories.

3. **Developer Experience**: No caching on templates, JSON, or static assets.
   Cache-Control: no-cache headers on all responses. Browser refresh = instant
   update.

4. **OSS Quality**: Configuration via `--templates-root` and `--defs-root`
   CLI args or `TEMPLATES_ROOT` / `DEFS_ROOT` env vars. Sensible defaults
   for port (8080). Clear startup banner with watched directories and URL.

## Project Structure

### Documentation (this feature)

```text
specs/001-thymeleaf-preview-tool/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/example/thymeleafpreview/
├── ThymeleafPreviewApplication.java    # Main entry point
├── config/
│   ├── PreviewProperties.java          # Configuration properties
│   ├── ThymeleafConfig.java            # Thymeleaf FileSystemResolver setup
│   └── WebMvcConfig.java               # Static resource mapping
├── controller/
│   ├── PreviewController.java          # /preview endpoint
│   └── CatalogController.java          # /catalog endpoint
├── service/
│   ├── TemplateService.java            # Template resolution logic
│   ├── DefinitionService.java          # JSON loading and merging
│   └── CatalogService.java             # File enumeration for catalog
├── model/
│   ├── PreviewRequest.java             # Request parameters
│   ├── TemplateInfo.java               # Catalog entry
│   └── DefinitionData.java             # Merged JSON data
└── exception/
    ├── TemplateNotFoundException.java
    └── InvalidPathException.java

src/main/resources/
├── application.yml                      # Default configuration
└── templates/
    └── preview-wrapper.html             # Wrapper template with th:insert

src/test/java/com/example/thymeleafpreview/
├── controller/
│   ├── PreviewControllerTest.java
│   └── CatalogControllerTest.java
├── service/
│   ├── TemplateServiceTest.java
│   ├── DefinitionServiceTest.java
│   └── CatalogServiceTest.java
└── integration/
    └── PreviewIntegrationTest.java

Dockerfile
pom.xml
```

**Structure Decision**: Single Spring Boot application. No frontend/backend
split needed since this is a server-rendered tool with minimal UI (Thymeleaf
catalog page). Tests organized by layer (controller, service, integration).

## Complexity Tracking

> No violations detected. All constitutional principles satisfied.

No entries required.
