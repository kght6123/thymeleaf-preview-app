package com.example.dialect;

import java.util.Collections;
import java.util.Map;

/**
 * Mock HTTP request object for template preview.
 *
 * This expression object provides access to HTTP request-like data
 * without requiring an actual HTTP request context.
 *
 * Usage in templates:
 *   ${#request.getHeader('Accept-Language')}
 *   ${#request.contextPath}
 *   ${#request.requestURI}
 *   ${#request.getAttribute('userId')}
 *
 * Configuration in global.json or page-specific JSON:
 * {
 *   "dialects": {
 *     "request": {
 *       "headers": {
 *         "Accept-Language": "en-US",
 *         "X-Custom-Header": "custom-value"
 *       },
 *       "attributes": {
 *         "userId": "12345"
 *       },
 *       "contextPath": "/app",
 *       "requestURI": "/products/list",
 *       "queryString": "page=1&size=10",
 *       "serverName": "localhost"
 *     }
 *   }
 * }
 */
public class MockHttpRequest {

    private final Map<String, Object> headers;
    private final Map<String, Object> attributes;
    private final String contextPath;
    private final String requestURI;
    private final String queryString;
    private final String serverName;

    @SuppressWarnings("unchecked")
    public MockHttpRequest(Map<String, Object> config) {
        this.headers = getMapConfig(config, "headers");
        this.attributes = getMapConfig(config, "attributes");
        this.contextPath = getStringConfig(config, "contextPath", "");
        this.requestURI = getStringConfig(config, "requestURI", "/");
        this.queryString = getStringConfig(config, "queryString", "");
        this.serverName = getStringConfig(config, "serverName", "localhost");
    }

    /**
     * Gets a header value by name.
     * @param name the header name (case-insensitive in real HTTP, but exact match here)
     * @return the header value, or null if not found
     */
    public String getHeader(String name) {
        Object value = headers.get(name);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets an attribute value by name.
     * @param name the attribute name
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Gets the context path.
     * @return the context path (e.g., "/app")
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Gets the request URI.
     * @return the request URI (e.g., "/products/list")
     */
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Gets the query string.
     * @return the query string (e.g., "page=1&size=10")
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Gets the server name.
     * @return the server name (e.g., "localhost")
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Gets the full request URL.
     * @return the full URL combining contextPath, requestURI, and queryString
     */
    public String getRequestURL() {
        StringBuilder url = new StringBuilder();
        url.append("http://").append(serverName);
        url.append(contextPath).append(requestURI);
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapConfig(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
