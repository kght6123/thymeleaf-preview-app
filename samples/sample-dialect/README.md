# Sample Preview Dialect

This is a sample custom Thymeleaf dialect for thymeleaf-preview-app that demonstrates how to create expression objects for template preview.

## Overview

When previewing Thymeleaf templates, you may need access to objects that are normally provided by your application at runtime (e.g., `HttpServletRequest`, custom utilities). This sample shows how to create mock implementations that work with thymeleaf-preview-app.

## Project Structure

```
sample-dialect/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/dialect/
    │   ├── SampleDialect.java              # Main dialect class
    │   ├── SampleExpressionObjectFactory.java  # Creates expression objects
    │   ├── MockHttpRequest.java            # #request expression object
    │   └── PreviewUtils.java               # #utils expression object
    └── resources/META-INF/services/
        └── org.thymeleaf.dialect.IDialect  # ServiceLoader config
```

## Expression Objects

### #request

Mock HTTP request object for accessing request data.

```html
<!-- Get header value -->
<span th:text="${#request.getHeader('Accept-Language')}">en-US</span>

<!-- Get attribute -->
<span th:text="${#request.getAttribute('userId')}">12345</span>

<!-- Get context path -->
<a th:href="${#request.contextPath + '/products'}">Products</a>

<!-- Get full URL -->
<span th:text="${#request.requestURL}">http://localhost/app/page</span>
```

### #utils

Utility functions for common template operations.

```html
<!-- Format currency -->
<span th:text="${#utils.formatCurrency(product.price)}">$1,234.56</span>

<!-- Format number -->
<span th:text="${#utils.formatNumber(viewCount)}">1,234,567</span>

<!-- Truncate text -->
<p th:text="${#utils.truncate(description, 100)}">Long text...</p>

<!-- URL encode -->
<a th:href="'/search?q=' + ${#utils.urlEncode(query)}">Search</a>

<!-- Default value -->
<span th:text="${#utils.defaultIfEmpty(nickname, 'Anonymous')}">Name</span>

<!-- Translation -->
<span th:text="${#utils.translate('greeting')}">Hello</span>

<!-- Newline to BR -->
<p th:utext="${#utils.nl2br(comment)}">Multi-line text</p>
```

## Configuration

Configure the dialect in your definition JSON files (global.json or page-specific):

```json
{
  "dialects": {
    "request": {
      "headers": {
        "Accept-Language": "en-US",
        "X-Custom-Header": "custom-value"
      },
      "attributes": {
        "userId": "12345",
        "sessionId": "abc-123"
      },
      "contextPath": "/app",
      "requestURI": "/products/list",
      "queryString": "page=1&size=10",
      "serverName": "localhost"
    },
    "utils": {
      "locale": "en-US",
      "currencySymbol": "$",
      "translations": {
        "greeting": "Hello",
        "farewell": "Goodbye",
        "submit": "Submit"
      }
    }
  }
}
```

## Build

```bash
cd samples/sample-dialect
mvn package
```

This creates `target/sample-preview-dialect-1.0.0-SNAPSHOT.jar`.

## Usage

### Option 1: Using loader.path (Recommended)

```bash
java -Dloader.path=/path/to/sample-preview-dialect-1.0.0-SNAPSHOT.jar \
    -jar thymeleaf-preview-1.0.0-SNAPSHOT.jar \
    --preview.templates-root=/path/to/templates \
    --preview.defs-root=/path/to/defs
```

### Option 2: Using startup script

Create a startup script:

```bash
#!/bin/bash
PREVIEW_JAR="/path/to/thymeleaf-preview-1.0.0-SNAPSHOT.jar"
DIALECT_JAR="/path/to/sample-preview-dialect-1.0.0-SNAPSHOT.jar"

java -Dloader.path="$DIALECT_JAR" \
    -jar "$PREVIEW_JAR" \
    --preview.templates-root=/path/to/templates \
    --preview.defs-root=/path/to/defs \
    --server.port=8080
```

## Creating Your Own Dialect

1. **Copy this sample project** as a starting point
2. **Rename packages** to match your organization
3. **Create expression objects** for your specific needs
4. **Update ServiceLoader config** with your dialect class name
5. **Build and use** with thymeleaf-preview-app

### Key Files to Modify

| File | Purpose |
|------|---------|
| `pom.xml` | Update groupId, artifactId, name |
| `SampleDialect.java` | Rename and update DIALECT_NAME |
| `SampleExpressionObjectFactory.java` | Add/modify expression object names |
| `MockHttpRequest.java` | Customize request mock |
| `PreviewUtils.java` | Add your utility methods |
| `META-INF/services/...IDialect` | Update with your dialect class |

### ServiceLoader Auto-Discovery

The dialect is automatically discovered by thymeleaf-preview-app via Java ServiceLoader. The key is the file:

```
src/main/resources/META-INF/services/org.thymeleaf.dialect.IDialect
```

This file must contain the fully qualified class name of your dialect:

```
com.example.dialect.SampleDialect
```

## Tips

- Use `provided` scope for thymeleaf dependency (it's supplied by thymeleaf-preview-app)
- Expression objects can read configuration from `__dialects__` context variable
- Keep your dialect JAR small - it's loaded at runtime
- Test your expression objects with unit tests before integration

## License

This sample is provided as part of thymeleaf-preview-app and is available under the same license.
