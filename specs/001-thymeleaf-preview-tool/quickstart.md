# Quick Start: Thymeleaf Preview Tool

Get your first template preview running in under 3 minutes.

## Prerequisites

- Java 21 or later
- Maven 3.8+ (or use included Maven wrapper)
- A directory containing Thymeleaf templates

## Installation

### Option 1: Run from JAR

```bash
# Download the latest release
curl -L -o thymeleaf-preview.jar \
  https://github.com/your-org/thymeleaf-preview-app/releases/latest/download/thymeleaf-preview.jar

# Run with your template directory
java -jar thymeleaf-preview.jar \
  --templates-root=/path/to/your/templates \
  --defs-root=/path/to/your/definitions
```

### Option 2: Run with Docker

```bash
docker run -p 8080:8080 \
  -v /path/to/your/templates:/templates:ro \
  -v /path/to/your/definitions:/defs \
  your-org/thymeleaf-preview:latest
```

### Option 3: Build from Source

```bash
git clone https://github.com/your-org/thymeleaf-preview-app.git
cd thymeleaf-preview-app
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--templates-root=/path/to/templates --defs-root=/path/to/defs"
```

## Quick Setup

### 1. Create a sample template directory

```bash
mkdir -p ~/preview-demo/templates/pages
mkdir -p ~/preview-demo/defs
```

### 2. Create a sample template

```bash
cat > ~/preview-demo/templates/pages/hello.html << 'EOF'
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Default Title</title>
</head>
<body>
    <h1 th:text="${greeting}">Hello</h1>
    <p th:text="${message}">Welcome to the preview tool.</p>
</body>
</html>
EOF
```

### 3. Create definition files

**global.json** (applies to all templates):
```bash
cat > ~/preview-demo/defs/global.json << 'EOF'
{
  "css": [],
  "js": [],
  "fixtures": {
    "siteName": "My Site"
  }
}
EOF
```

**Template-specific definition**:
```bash
mkdir -p ~/preview-demo/defs/pages
cat > ~/preview-demo/defs/pages/hello.json << 'EOF'
{
  "fixtures": {
    "title": "Hello Page",
    "greeting": "Welcome!",
    "message": "This is your first Thymeleaf preview."
  }
}
EOF
```

### 4. Start the preview server

```bash
java -jar thymeleaf-preview.jar \
  --templates-root=~/preview-demo/templates \
  --defs-root=~/preview-demo/defs
```

You should see:

```
===================================
 Thymeleaf Preview Tool v1.0.0
===================================
 Templates: /Users/you/preview-demo/templates
 Definitions: /Users/you/preview-demo/defs
 Server: http://localhost:8080
===================================
```

### 5. Open your browser

- **Preview template**: http://localhost:8080/preview?tpl=pages/hello.html
- **Browse catalog**: http://localhost:8080/catalog

## Configuration Options

| Option | Env Var | Default | Description |
|--------|---------|---------|-------------|
| `--templates-root` | `TEMPLATES_ROOT` | (required) | Path to template directory |
| `--defs-root` | `DEFS_ROOT` | (required) | Path to JSON definitions |
| `--server.port` | `SERVER_PORT` | 8080 | HTTP server port |

## Preview URL Parameters

| Parameter | Required | Description |
|-----------|----------|-------------|
| `tpl` | Yes | Relative path to template (e.g., `pages/index.html`) |
| `fragment` | No | Fragment name to render (e.g., `header`) |

**Examples**:
- Full template: `/preview?tpl=pages/index.html`
- Fragment only: `/preview?tpl=components/navbar.html&fragment=menu`

## Hot Reload

The preview tool automatically reflects file changes:

1. Edit any `.html` template
2. Refresh your browser
3. See the updated content immediately

No server restart required. This works for:
- Thymeleaf templates
- JSON definition files
- CSS and JavaScript files

## Troubleshooting

### "Template not found" error

- Check that the `tpl` parameter matches the relative path from `templatesRoot`
- Ensure the file has a `.html` extension
- Verify the file exists: `ls /path/to/templates/pages/hello.html`

### "Invalid JSON" error

- Validate your JSON syntax: `cat defs/global.json | python -m json.tool`
- Check for trailing commas or unquoted keys
- The error message includes the file path and line number

### Port already in use

```bash
java -jar thymeleaf-preview.jar \
  --templates-root=/path/to/templates \
  --defs-root=/path/to/defs \
  --server.port=3000
```

## Next Steps

- Read the [Data Model](./data-model.md) for JSON schema details
- Check the [API Contracts](./contracts/openapi.yaml) for all endpoints
- Review the [Implementation Plan](./plan.md) for architecture decisions
