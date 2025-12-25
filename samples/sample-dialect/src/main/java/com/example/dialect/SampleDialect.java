package com.example.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * Sample custom Thymeleaf dialect for thymeleaf-preview-app.
 *
 * This dialect demonstrates how to create custom expression objects
 * that can be used in Thymeleaf templates with the # prefix.
 *
 * Example usage in templates:
 * - ${#request.getHeader('X-Custom-Header')}
 * - ${#utils.formatCurrency(1234.56)}
 * - ${#utils.truncate('Long text...', 10)}
 *
 * The dialect is auto-discovered via ServiceLoader when the JAR is
 * added to the classpath using the loader.path option.
 */
public class SampleDialect extends AbstractDialect implements IExpressionObjectDialect {

    public static final String DIALECT_NAME = "Sample Preview";

    private final IExpressionObjectFactory expressionObjectFactory;

    public SampleDialect() {
        super(DIALECT_NAME);
        this.expressionObjectFactory = new SampleExpressionObjectFactory();
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return this.expressionObjectFactory;
    }
}
