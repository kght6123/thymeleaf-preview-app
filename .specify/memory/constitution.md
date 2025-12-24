<!--
================================================================================
SYNC IMPACT REPORT
================================================================================
Version change: N/A (initial) → 1.0.0
Modified principles: N/A (initial adoption)
Added sections:
  - Core Principles (4 principles)
  - Directory Separation section
  - Development Workflow section
  - Governance section
Removed sections: N/A
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ no update needed (generic gates, will
    be filled per constitution)
  - .specify/templates/spec-template.md ✅ no update needed (generic structure)
  - .specify/templates/tasks-template.md ✅ no update needed (generic structure)
  - .specify/templates/checklist-template.md ✅ no update needed (generic structure)
  - .specify/templates/agent-file-template.md ✅ no update needed (generic structure)
Follow-up TODOs: None
================================================================================
-->

# Thymeleaf Preview App Constitution

## Core Principles

### I. Non-intrusive (非侵襲性)

Existing project structures and template directories MUST remain completely
unpolluted by the preview system.

- **MUST NOT** add any configuration files, metadata, or settings to the
  target `templatesRoot` directory
- **MUST NOT** modify existing template files or directory structures
- All preview-specific configuration and definitions MUST reside in a
  separate `defsRoot` directory
- The preview application operates as an external observer, never as an
  invasive modifier

**Rationale**: Users integrate this tool into existing projects that may have
strict version control, CI/CD pipelines, or linting rules. Any file pollution
would break these workflows and reduce adoption.

### II. Performance & Scalability (パフォーマンスとスケーラビリティ)

The system MUST maintain responsive performance even with codebases containing
tens of thousands of template files.

- Template enumeration and search MUST use lazy loading or pagination
  strategies to avoid loading all files into memory
- Catalog/index views MUST render incrementally or use virtualization
  techniques to prevent UI freezes
- File watching MUST be efficient (e.g., debounced, selective) to avoid
  excessive CPU usage
- Rendering MUST be optimized with caching where appropriate, but cache
  invalidation MUST be reliable

**Rationale**: Enterprise projects often contain 10,000+ templates across
multiple modules. A preview tool that slows down at scale becomes unusable
and forces developers to resort to slower alternatives.

### III. Developer Experience (開発者体験)

The system MUST prioritize rapid feedback loops and flexibility in project
structure.

- **Hot reload** is MANDATORY: HTML, CSS, and JavaScript changes MUST reflect
  in the browser without server restart
- Template data changes (in `defsRoot`) MUST also trigger hot reload
- The system MUST handle irregular directory structures gracefully, including:
  - Multiple template roots
  - Non-standard path mappings
  - Symlinked directories
- Error messages MUST be clear, actionable, and include file paths and line
  numbers where applicable

**Rationale**: Developer productivity depends on instant feedback. Requiring
restarts or manual refreshes breaks flow state and significantly slows
iteration cycles.

### IV. OSS Quality (OSSとしての品質)

The system MUST be usable by any developer within 3 minutes of discovery.

- All configuration MUST be achievable via command-line arguments or
  environment variables (no mandatory config files)
- Default values MUST be sensible for common use cases
- Startup logs MUST clearly indicate:
  - Which directories are being watched
  - What URL to access
  - Any warnings about missing or invalid configurations
- Error states MUST provide clear remediation guidance
- Documentation MUST include a working quick-start example

**Rationale**: Open source adoption depends on first impressions. If setup
is complex or documentation unclear, developers will abandon the tool before
experiencing its value.

## Directory Separation

The clear separation between `templatesRoot` and `defsRoot` is a fundamental
architectural constraint:

| Directory | Purpose | Mutability |
|-----------|---------|------------|
| `templatesRoot` | Existing Thymeleaf templates (read-only by this tool) | NEVER modified |
| `defsRoot` | Preview definitions, mock data, configuration | Fully managed |

**Enforcement**: Code reviews and tests MUST verify that no write operations
target paths within or derived from `templatesRoot`.

## Development Workflow

### Change Verification

Before merging any PR:

1. Confirm no files are written to `templatesRoot` (Principle I)
2. Verify performance with large file counts using benchmark tests (Principle II)
3. Test hot reload for all supported file types (Principle III)
4. Validate startup works with only CLI args/env vars (Principle IV)

### Logging Standards

- INFO level: Startup messages, watched directories, server URLs
- DEBUG level: File change events, template resolution paths
- WARN level: Missing optional config, deprecated usage
- ERROR level: Fatal conditions with remediation steps

## Governance

This constitution is the authoritative reference for architectural and design
decisions in the Thymeleaf Preview App project.

### Amendment Process

1. Propose changes via GitHub Issue with `constitution` label
2. Document rationale and impact analysis
3. Obtain maintainer approval
4. Update constitution with version increment
5. Update any affected documentation and templates

### Versioning Policy

- **MAJOR**: Principle removal or fundamental redefinition
- **MINOR**: New principle added or existing principle materially expanded
- **PATCH**: Wording clarifications, typo fixes, non-semantic refinements

### Compliance

- All PRs MUST demonstrate compliance with applicable principles
- Complexity that violates principles MUST be explicitly justified in PR
  description and approved by maintainers
- Runtime development guidance is documented in project README and inline
  code comments

**Version**: 1.0.0 | **Ratified**: 2025-12-24 | **Last Amended**: 2025-12-24
