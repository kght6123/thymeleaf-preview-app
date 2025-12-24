# Feature Specification: Thymeleaf Preview Tool

**Feature Branch**: `001-thymeleaf-preview-tool`
**Created**: 2025-12-24
**Status**: Draft
**Input**: User description: "Spring Bootアプリケーションとして動作する、Thymeleafテンプレート専用のローカルプレビューツールを構築したい。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Preview Single Template (Priority: P1)

As a frontend developer, I want to preview a specific Thymeleaf template with
mock data so that I can verify the HTML rendering without running the full
application.

**Why this priority**: This is the core functionality that delivers immediate
value. Without template preview, the tool has no purpose.

**Independent Test**: Can be fully tested by starting the application with
templatesRoot and defsRoot paths, then accessing `/preview?tpl=path/to/template.html`
to see the rendered output.

**Acceptance Scenarios**:

1. **Given** the application is running with valid templatesRoot and defsRoot,
   **When** I access `/preview?tpl=pages/index.html`,
   **Then** I see the rendered HTML with mock data applied.

2. **Given** a template uses Thymeleaf fragment syntax,
   **When** I access `/preview?tpl=components/header.html&fragment=navbar`,
   **Then** I see only the specified fragment rendered.

3. **Given** a template path does not exist,
   **When** I access `/preview?tpl=nonexistent.html`,
   **Then** I see a clear error message indicating the template was not found.

---

### User Story 2 - Data Injection from JSON (Priority: P1)

As a frontend developer, I want my templates to receive mock data from JSON
files so that I can test various data scenarios without backend integration.

**Why this priority**: Templates without data cannot demonstrate real
functionality. This is essential for meaningful previews.

**Independent Test**: Can be tested by creating JSON files in defsRoot and
verifying that template variables are populated correctly.

**Acceptance Scenarios**:

1. **Given** a `global.json` exists in defsRoot with CSS/JS paths,
   **When** any template is previewed,
   **Then** the global CSS and JS paths are available to the template.

2. **Given** a template-specific JSON exists at `defsRoot/pages/index.json`,
   **When** I preview `pages/index.html`,
   **Then** the template receives merged data from both global.json and the
   specific JSON file.

3. **Given** a template-specific JSON has a key that conflicts with global.json,
   **When** I preview the template,
   **Then** the template-specific value takes precedence over the global value.

---

### User Story 3 - Static Asset Serving (Priority: P2)

As a frontend developer, I want static files (CSS, JS, images) from my template
directory to be accessible so that my previews look complete.

**Why this priority**: Without static assets, previews cannot display proper
styling or functionality. This enables realistic preview rendering.

**Independent Test**: Can be tested by placing a CSS file in templatesRoot and
verifying it loads via `/assets/` URL.

**Acceptance Scenarios**:

1. **Given** a CSS file exists at `templatesRoot/css/style.css`,
   **When** I access `/assets/css/style.css`,
   **Then** I receive the CSS file content with correct MIME type.

2. **Given** an image file exists at `templatesRoot/images/logo.png`,
   **When** I access `/assets/images/logo.png`,
   **Then** I receive the image file with correct MIME type.

---

### User Story 4 - Template Catalog (Priority: P2)

As a frontend developer, I want to browse and search all available templates
so that I can quickly find and preview specific templates.

**Why this priority**: With many templates, manual path entry becomes tedious.
A catalog improves discoverability and productivity.

**Independent Test**: Can be tested by accessing `/catalog` and verifying the
template list is displayed and searchable.

**Acceptance Scenarios**:

1. **Given** multiple templates exist in templatesRoot,
   **When** I access `/catalog`,
   **Then** I see a list of all template files.

2. **Given** I am on the catalog page,
   **When** I enter a search term,
   **Then** the template list is filtered to show only matching templates.

3. **Given** I see a template in the catalog,
   **When** I click on it,
   **Then** I am taken to the preview page for that template.

---

### User Story 5 - Hot Reload (Priority: P3)

As a frontend developer, I want my browser to show updated content when I
modify template or data files so that I can iterate quickly.

**Why this priority**: While not blocking basic functionality, hot reload
dramatically improves the development workflow.

**Independent Test**: Can be tested by modifying a template file and refreshing
the browser to see changes immediately.

**Acceptance Scenarios**:

1. **Given** I am viewing a template preview,
   **When** I modify the template HTML file and refresh the browser,
   **Then** I see the updated content without restarting the server.

2. **Given** I am viewing a template preview,
   **When** I modify a JSON data file in defsRoot and refresh the browser,
   **Then** I see the template with the updated data.

3. **Given** I am viewing a template preview,
   **When** I modify a CSS file in templatesRoot and refresh the browser,
   **Then** I see the updated styles.

---

### Edge Cases

- What happens when templatesRoot or defsRoot paths are invalid at startup?
  → Application displays a clear error message and exits gracefully.
- What happens when a JSON file contains invalid JSON syntax?
  → Preview displays a clear error indicating which file has the syntax error.
- What happens when a template references a variable not present in JSON data?
  → Template renders with empty/null value and logs a warning.
- What happens when catalog is accessed with 10,000+ template files?
  → Catalog uses pagination or lazy loading to remain responsive.
- What happens when template path contains path traversal attempts (../)?
  → System sanitizes paths and rejects access outside templatesRoot.

## Requirements *(mandatory)*

### Functional Requirements

**Startup & Configuration**:

- **FR-001**: System MUST accept `templatesRoot` path as a startup parameter
  (command-line argument or environment variable).
- **FR-002**: System MUST accept `defsRoot` path as a startup parameter
  (command-line argument or environment variable).
- **FR-003**: System MUST validate that both paths exist and are readable
  at startup, exiting with a clear error if not.
- **FR-004**: System MUST provide sensible defaults for optional configuration
  (port, host, etc.) to enable quick startup.

**Preview Endpoint**:

- **FR-005**: System MUST provide a `/preview` endpoint that accepts a `tpl`
  query parameter for template path.
- **FR-006**: System MUST resolve template paths relative to templatesRoot.
- **FR-007**: System MUST support optional `fragment` query parameter for
  rendering specific Thymeleaf fragments.
- **FR-008**: System MUST return appropriate HTTP error codes (404 for not
  found, 500 for render errors) with descriptive messages.

**Data Injection**:

- **FR-009**: System MUST load `global.json` from defsRoot if it exists and
  make its data available to all templates.
- **FR-010**: System MUST load template-specific JSON files from defsRoot
  based on template path (e.g., `pages/index.json` for `pages/index.html`).
- **FR-011**: System MUST merge global and template-specific data, with
  template-specific values taking precedence.
- **FR-012**: System MUST support nested JSON structures for complex data.

**Static Files**:

- **FR-013**: System MUST serve files from templatesRoot under the `/assets/`
  URL path.
- **FR-014**: System MUST set appropriate Content-Type headers based on file
  extension.
- **FR-015**: System MUST NOT allow path traversal attacks via the assets
  endpoint.

**Catalog**:

- **FR-016**: System MUST provide a `/catalog` endpoint displaying all
  available template files.
- **FR-017**: System MUST provide search/filter functionality in the catalog.
- **FR-018**: Catalog MUST link each template to its preview URL.
- **FR-019**: Catalog MUST remain responsive with large numbers of templates
  (10,000+).

**Hot Reload**:

- **FR-020**: System MUST NOT cache templates, ensuring each request reads
  the current file content.
- **FR-021**: System MUST NOT cache JSON data files, ensuring each request
  uses current data.
- **FR-022**: Static files MUST be served with no-cache headers during
  development to enable instant updates.

### Key Entities

- **Template**: An HTML file using Thymeleaf syntax, located within
  templatesRoot. Identified by its relative path.
- **Definition (JSON)**: A JSON file in defsRoot containing mock data for
  template rendering. Can be global (global.json) or template-specific
  (matching template path structure).
- **Fragment**: A named section within a template that can be rendered
  independently using Thymeleaf fragment syntax.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can start the application and view their first template
  preview within 3 minutes of downloading the tool.
- **SC-002**: Template preview renders and displays within 2 seconds for
  templates up to 100KB in size.
- **SC-003**: Catalog page loads and displays template list within 3 seconds
  for directories containing up to 10,000 templates.
- **SC-004**: File changes (HTML, CSS, JS, JSON) are reflected in the browser
  immediately upon page refresh without server restart.
- **SC-005**: 100% of preview requests for valid templates succeed without
  errors.
- **SC-006**: Error messages for common issues (invalid path, missing file,
  bad JSON) are actionable and include specific file paths.

## Assumptions

- Users have a Java runtime environment available (required for Spring Boot).
- Template files use standard Thymeleaf syntax compatible with Thymeleaf 3.x.
- JSON files in defsRoot use valid JSON syntax (UTF-8 encoding).
- The tool is used for local development only; no authentication is required.
- Static files follow common web conventions for file extensions and MIME types.
