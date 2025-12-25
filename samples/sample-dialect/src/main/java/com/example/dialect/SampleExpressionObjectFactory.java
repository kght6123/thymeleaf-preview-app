package com.example.dialect;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating expression objects provided by this dialect.
 *
 * Expression objects are accessed in templates using the # prefix:
 * - #request - Mock HTTP request object
 * - #utils - Utility functions for formatting, etc.
 *
 * Configuration is read from the "dialects" section of the definition JSON files.
 */
public class SampleExpressionObjectFactory implements IExpressionObjectFactory {

    public static final String REQUEST_EXPRESSION_OBJECT_NAME = "request";
    public static final String UTILS_EXPRESSION_OBJECT_NAME = "utils";

    private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES;

    static {
        Set<String> names = new LinkedHashSet<>();
        names.add(REQUEST_EXPRESSION_OBJECT_NAME);
        names.add(UTILS_EXPRESSION_OBJECT_NAME);
        ALL_EXPRESSION_OBJECT_NAMES = Collections.unmodifiableSet(names);
    }

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        // Get dialect configuration from context variables
        Map<String, Object> dialectsConfig = getDialectsConfig(context);

        if (REQUEST_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            Map<String, Object> requestConfig = getSubConfig(dialectsConfig, "request");
            return new MockHttpRequest(requestConfig);
        }

        if (UTILS_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            Map<String, Object> utilsConfig = getSubConfig(dialectsConfig, "utils");
            return new PreviewUtils(utilsConfig);
        }

        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        // Expression objects are cacheable within a single template processing
        return true;
    }

    /**
     * Retrieves the dialects configuration from the Thymeleaf context.
     * This configuration comes from the "dialects" section in definition JSON files.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getDialectsConfig(IExpressionContext context) {
        Object dialectsObj = context.getVariable("__dialects__");
        if (dialectsObj instanceof Map) {
            return (Map<String, Object>) dialectsObj;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets a sub-configuration map for a specific dialect component.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getSubConfig(Map<String, Object> dialectsConfig, String key) {
        Object subConfig = dialectsConfig.get(key);
        if (subConfig instanceof Map) {
            return (Map<String, Object>) subConfig;
        }
        return Collections.emptyMap();
    }
}
