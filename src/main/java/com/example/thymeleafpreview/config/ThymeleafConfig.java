package com.example.thymeleafpreview.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@ConditionalOnProperty(name = "preview.templates-root")
public class ThymeleafConfig {

    private final PreviewProperties previewProperties;

    public ThymeleafConfig(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    /**
     * File-based template resolver for external templates in templatesRoot.
     * Caching is disabled to enable hot reload.
     */
    @Bean
    public ITemplateResolver externalTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(previewProperties.getTemplatesRoot().toAbsolutePath().toString() + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);  // Hot reload: always read from disk
        resolver.setCheckExistence(true);
        resolver.setOrder(1);  // Higher priority than classpath resolver
        return resolver;
    }

    /**
     * Classpath template resolver for internal templates (error.html, preview-wrapper.html).
     * These are bundled with the application.
     */
    @Bean
    public ITemplateResolver classpathTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);  // Hot reload during development
        resolver.setCheckExistence(true);
        resolver.setOrder(2);  // Fallback after external resolver
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(
            ITemplateResolver externalTemplateResolver,
            ITemplateResolver classpathTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(externalTemplateResolver);
        engine.addTemplateResolver(classpathTemplateResolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
