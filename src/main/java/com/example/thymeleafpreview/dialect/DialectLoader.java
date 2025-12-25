package com.example.thymeleafpreview.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.IDialect;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Loads custom Thymeleaf dialects from external JARs using ServiceLoader.
 *
 * External dialects should register themselves via META-INF/services/org.thymeleaf.dialect.IDialect
 */
@Component
public class DialectLoader {

    private static final Logger log = LoggerFactory.getLogger(DialectLoader.class);

    /**
     * Loads all IDialect implementations found via ServiceLoader.
     * This allows external JARs to provide custom dialects without modifying this application.
     *
     * @return list of discovered dialects
     */
    public List<IDialect> loadExternalDialects() {
        ServiceLoader<IDialect> loader = ServiceLoader.load(IDialect.class);

        List<IDialect> dialects = StreamSupport
            .stream(loader.spliterator(), false)
            .collect(Collectors.toList());

        if (dialects.isEmpty()) {
            log.debug("No external dialects found via ServiceLoader");
        } else {
            log.info("Loaded {} external dialect(s):", dialects.size());
            for (IDialect dialect : dialects) {
                log.info("  - {} (prefix: {})",
                    dialect.getName(),
                    dialect instanceof org.thymeleaf.dialect.IProcessorDialect
                        ? ((org.thymeleaf.dialect.IProcessorDialect) dialect).getPrefix()
                        : "N/A");
            }
        }

        return dialects;
    }
}
