# Tasks: Thymeleaf Preview Tool

**Input**: Design documents from `/specs/001-thymeleaf-preview-tool/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: ユニットテスト、コントローラーテスト、統合テストを含む（ユーザーリクエストにより追加）

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/example/thymeleafpreview/` for Java sources
- **Resources**: `src/main/resources/` for configuration and templates
- **Tests**: `src/test/java/com/example/thymeleafpreview/` for test sources

## Testing Strategy

- **Unit Tests**: JUnit 5 + Mockito for service layer isolation
- **Controller Tests**: MockMvc for endpoint testing without full server
- **Integration Tests**: @SpringBootTest with real file system fixtures

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven project with pom.xml including Spring Boot 3.x, Thymeleaf, Jackson, JUnit 5, Mockito dependencies
- [x] T002 Create main application class in src/main/java/com/example/thymeleafpreview/ThymeleafPreviewApplication.java
- [x] T003 [P] Create application.yml with default configuration in src/main/resources/application.yml
- [x] T004 [P] Create Dockerfile for containerized deployment in Dockerfile
- [x] T005 [P] Create test resources directory structure in src/test/resources/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Create PreviewProperties configuration class with templatesRoot and defsRoot in src/main/java/com/example/thymeleafpreview/config/PreviewProperties.java
- [x] T007 Implement path validation in PreviewProperties to check directories exist at startup
- [x] T008 [P] Create TemplateNotFoundException exception class in src/main/java/com/example/thymeleafpreview/exception/TemplateNotFoundException.java
- [x] T009 [P] Create InvalidPathException exception class in src/main/java/com/example/thymeleafpreview/exception/InvalidPathException.java
- [x] T010 Create GlobalExceptionHandler with @ControllerAdvice in src/main/java/com/example/thymeleafpreview/exception/GlobalExceptionHandler.java
- [x] T011 Create error.html template for error display in src/main/resources/templates/error.html
- [x] T012 Configure FileTemplateResolver for external templates in src/main/java/com/example/thymeleafpreview/config/ThymeleafConfig.java
- [x] T013 [P] Create test fixtures: sample templates in src/test/resources/fixtures/templates/
- [x] T014 [P] Create test fixtures: sample JSON definitions in src/test/resources/fixtures/defs/

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1+2 - Preview with Data Injection (Priority: P1) 🎯 MVP

**Goal**: Preview Thymeleaf templates with mock data from JSON definitions

**Independent Test**: Start application with --templates-root and --defs-root, access /preview?tpl=path/to/template.html to see rendered output with data

**Note**: User Stories 1 and 2 are combined as they are interdependent (preview requires data injection to be meaningful)

### Tests for User Story 1+2

> **TDD Approach**: Write tests FIRST, ensure they FAIL, then implement

- [x] T015 [P] [US1] Unit test for TemplateService path resolution in src/test/java/com/example/thymeleafpreview/service/TemplateServiceTest.java
- [x] T016 [P] [US1] Unit test for TemplateService path traversal prevention in src/test/java/com/example/thymeleafpreview/service/TemplateServiceTest.java
- [x] T017 [P] [US2] Unit test for DefinitionService JSON loading in src/test/java/com/example/thymeleafpreview/service/DefinitionServiceTest.java
- [x] T018 [P] [US2] Unit test for DefinitionService JSON merging (global + specific) in src/test/java/com/example/thymeleafpreview/service/DefinitionServiceTest.java
- [x] T019 [P] [US1] Controller test for /preview endpoint success in src/test/java/com/example/thymeleafpreview/controller/PreviewControllerTest.java
- [x] T020 [P] [US1] Controller test for /preview endpoint 404 error in src/test/java/com/example/thymeleafpreview/controller/PreviewControllerTest.java
- [x] T021 [P] [US1] Controller test for /preview with fragment parameter in src/test/java/com/example/thymeleafpreview/controller/PreviewControllerTest.java
- [x] T022 [US1] Integration test for full preview flow in src/test/java/com/example/thymeleafpreview/integration/PreviewIntegrationTest.java

### Implementation for User Story 1+2

- [x] T023 [P] [US1] Create PreviewRequest model class in src/main/java/com/example/thymeleafpreview/model/PreviewRequest.java
- [x] T024 [P] [US2] Create DefinitionData model class in src/main/java/com/example/thymeleafpreview/model/DefinitionData.java
- [x] T025 [US1] Create TemplateService with secure path resolution in src/main/java/com/example/thymeleafpreview/service/TemplateService.java
- [x] T026 [US2] Create DefinitionService with JSON loading and merging in src/main/java/com/example/thymeleafpreview/service/DefinitionService.java
- [x] T027 [US1] Create preview-wrapper.html template with th:insert and JS injection in src/main/resources/templates/preview-wrapper.html
- [x] T028 [US1] Create PreviewController with /preview endpoint in src/main/java/com/example/thymeleafpreview/controller/PreviewController.java
- [x] T029 [US1] Add fragment rendering support to PreviewController (fragment query parameter)
- [x] T030 [US2] Integrate DefinitionService into PreviewController for data injection
- [x] T031 [US1] Add path traversal prevention in TemplateService.resolveSecurely()
- [x] T032 [US1] Verify all unit tests pass for TemplateService
- [x] T033 [US2] Verify all unit tests pass for DefinitionService
- [x] T034 [US1] Verify all controller tests pass for PreviewController
- [x] T035 [US1] Verify integration test passes for preview flow

**Checkpoint**: User Story 1+2 (MVP) should be fully functional - preview templates with mock data

---

## Phase 4: User Story 3 - Static Asset Serving (Priority: P2)

**Goal**: Serve static files (CSS, JS, images) from templatesRoot under /assets/

**Independent Test**: Place CSS file in templatesRoot, access /assets/path/to/file.css to receive content with correct MIME type

### Tests for User Story 3

- [x] T036 [P] [US3] Controller test for /assets/** serving CSS file in src/test/java/com/example/thymeleafpreview/controller/AssetControllerTest.java
- [x] T037 [P] [US3] Controller test for /assets/** serving image file in src/test/java/com/example/thymeleafpreview/controller/AssetControllerTest.java
- [x] T038 [P] [US3] Controller test for /assets/** path traversal prevention in src/test/java/com/example/thymeleafpreview/controller/AssetControllerTest.java
- [x] T039 [US3] Integration test for static asset serving with no-cache headers in src/test/java/com/example/thymeleafpreview/integration/AssetIntegrationTest.java

### Implementation for User Story 3

- [x] T040 [US3] Create WebMvcConfig to map /assets/** to file:templatesRoot/ in src/main/java/com/example/thymeleafpreview/config/WebMvcConfig.java
- [x] T041 [US3] Configure no-cache headers for static resources in WebMvcConfig
- [x] T042 [US3] Add path traversal protection for assets endpoint in WebMvcConfig
- [x] T043 [US3] Verify all controller tests pass for assets
- [x] T044 [US3] Verify integration test passes for asset serving

**Checkpoint**: Static assets now accessible via /assets/** URLs

---

## Phase 5: User Story 4 - Template Catalog (Priority: P2)

**Goal**: Browse and search all templates in a catalog UI

**Independent Test**: Access /catalog to see list of all templates, use search to filter, click template to preview

### Tests for User Story 4

- [x] T045 [P] [US4] Unit test for CatalogService file enumeration in src/test/java/com/example/thymeleafpreview/service/CatalogServiceTest.java
- [x] T046 [P] [US4] Unit test for CatalogService search filtering in src/test/java/com/example/thymeleafpreview/service/CatalogServiceTest.java
- [x] T047 [P] [US4] Unit test for CatalogService pagination in src/test/java/com/example/thymeleafpreview/service/CatalogServiceTest.java
- [x] T048 [P] [US4] Controller test for /catalog endpoint in src/test/java/com/example/thymeleafpreview/controller/CatalogControllerTest.java
- [x] T049 [P] [US4] Controller test for /catalog with search parameter in src/test/java/com/example/thymeleafpreview/controller/CatalogControllerTest.java
- [x] T050 [US4] Integration test for catalog with large file count in src/test/java/com/example/thymeleafpreview/integration/CatalogIntegrationTest.java

### Implementation for User Story 4

- [x] T051 [P] [US4] Create TemplateInfo model class in src/main/java/com/example/thymeleafpreview/model/TemplateInfo.java
- [x] T052 [P] [US4] Create CatalogPage model class in src/main/java/com/example/thymeleafpreview/model/CatalogPage.java
- [x] T053 [US4] Create CatalogService with Files.walk() streaming in src/main/java/com/example/thymeleafpreview/service/CatalogService.java
- [x] T054 [US4] Add search/filter functionality to CatalogService
- [x] T055 [US4] Add pagination support to CatalogService for large directories
- [x] T056 [US4] Create CatalogController with /catalog endpoint in src/main/java/com/example/thymeleafpreview/controller/CatalogController.java
- [x] T057 [US4] Create catalog.html template with search and template list in src/main/resources/templates/catalog.html
- [x] T058 [US4] Verify all unit tests pass for CatalogService
- [x] T059 [US4] Verify all controller tests pass for CatalogController
- [x] T060 [US4] Verify integration test passes for catalog

**Checkpoint**: Catalog UI functional with search and pagination

---

## Phase 6: User Story 5 - Hot Reload (Priority: P3)

**Goal**: File changes reflected on browser refresh without server restart

**Independent Test**: Edit template/JSON/CSS file, refresh browser, see updated content immediately

### Tests for User Story 5

- [x] T061 [US5] Integration test for template hot reload in src/test/java/com/example/thymeleafpreview/integration/HotReloadIntegrationTest.java
- [x] T062 [US5] Integration test for JSON definition hot reload in src/test/java/com/example/thymeleafpreview/integration/HotReloadIntegrationTest.java
- [x] T063 [US5] Integration test for static asset hot reload in src/test/java/com/example/thymeleafpreview/integration/HotReloadIntegrationTest.java

### Implementation for User Story 5

- [x] T064 [US5] Verify ThymeleafConfig has setCacheable(false) for templates
- [x] T065 [US5] Verify DefinitionService reads JSON fresh on each request (no caching)
- [x] T066 [US5] Add Cache-Control: no-cache headers to PreviewController responses
- [x] T067 [US5] Verify WebMvcConfig static resources have no-cache headers
- [x] T068 [US5] Verify all hot reload integration tests pass

**Checkpoint**: All file types reflect changes on browser refresh

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T069 Add startup banner with templatesRoot, defsRoot, and server URL in ThymeleafPreviewApplication.java
- [x] T070 [P] Add INFO logging for startup configuration in PreviewProperties
- [x] T071 [P] Add DEBUG logging for template resolution in TemplateService
- [x] T072 [P] Add WARN logging for missing optional JSON files in DefinitionService
- [x] T073 Run all tests and verify 100% pass rate
- [x] T074 Verify quickstart.md instructions work end-to-end
- [x] T075 Build and test Fat JAR with mvn package
- [x] T076 Build and test Docker image with docker build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1+2 (Phase 3)**: Depends on Foundational - MVP delivery
- **User Story 3 (Phase 4)**: Depends on Foundational - can run parallel to Phase 3
- **User Story 4 (Phase 5)**: Depends on Foundational - can run parallel to Phase 3/4
- **User Story 5 (Phase 6)**: Depends on Phases 3, 4, 5 (validates hot reload across all)
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1+2 (P1)**: Can start after Foundational (Phase 2) - Core MVP
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Independent of US1+2
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - Independent of US1+2, US3
- **User Story 5 (P3)**: Requires US1-4 complete - validates no-cache behavior across all

### Within Each User Story (TDD Order)

1. **Write tests FIRST** (unit tests → controller tests → integration tests)
2. Verify tests FAIL (red phase)
3. Models implementation
4. Services implementation
5. Controllers implementation
6. Verify tests PASS (green phase)
7. Refactor if needed

### Parallel Opportunities

- T003, T004, T005 can run in parallel (different files)
- T008, T009 can run in parallel (different exception classes)
- T013, T014 can run in parallel (different test fixtures)
- T015, T016, T017, T018 can run in parallel (different test files)
- T019, T020, T021 can run in parallel (same test file but different test methods)
- T023, T024 can run in parallel (different model classes)
- T036, T037, T038 can run in parallel (same test file but different test methods)
- T045, T046, T047, T048, T049 can run in parallel (different test methods)
- T051, T052 can run in parallel (different model classes)
- T070, T071, T072 can run in parallel (different logging additions)
- User Stories 3 and 4 can run in parallel after Foundational phase

---

## Parallel Example: Phase 3 Tests

```bash
# Launch all unit tests in parallel:
Task: "Unit test for TemplateService path resolution"
Task: "Unit test for TemplateService path traversal prevention"
Task: "Unit test for DefinitionService JSON loading"
Task: "Unit test for DefinitionService JSON merging"

# Launch all controller tests in parallel:
Task: "Controller test for /preview endpoint success"
Task: "Controller test for /preview endpoint 404 error"
Task: "Controller test for /preview with fragment parameter"
```

## Parallel Example: Phase 3 Models

```bash
# Launch model classes in parallel:
Task: "Create PreviewRequest model in src/.../model/PreviewRequest.java"
Task: "Create DefinitionData model in src/.../model/DefinitionData.java"
```

---

## Implementation Strategy

### MVP First (User Story 1+2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1+2 (Preview + Data Injection) with ALL tests
4. **STOP and VALIDATE**: Run all tests, verify 100% pass
5. Deploy/demo if ready - this is a usable MVP!

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1+2 with tests → All tests pass → Deploy/Demo (MVP!)
3. Add User Story 3 with tests → All tests pass → Deploy/Demo
4. Add User Story 4 with tests → All tests pass → Deploy/Demo
5. Add User Story 5 with tests → All tests pass → Deploy/Demo
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1+2 tests + implementation
   - Developer B: User Story 3 tests + implementation
   - Developer C: User Story 4 tests + implementation
3. Developer D: User Story 5 after others complete
4. Stories complete and integrate independently

---

## Test Summary

| Phase | Unit Tests | Controller Tests | Integration Tests | Total |
|-------|------------|------------------|-------------------|-------|
| Phase 3 (US1+2) | 4 | 3 | 1 | 8 |
| Phase 4 (US3) | 0 | 3 | 1 | 4 |
| Phase 5 (US4) | 3 | 2 | 1 | 6 |
| Phase 6 (US5) | 0 | 0 | 3 | 3 |
| **Total** | **7** | **8** | **6** | **21** |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **TDD**: Write tests first, verify they fail, then implement
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- US1 and US2 combined because data injection is essential for meaningful preview
- Hot reload (US5) is a verification phase ensuring no-cache works across all features
