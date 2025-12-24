package com.example.thymeleafpreview.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Web MVC configuration for static asset serving.
 * Maps /assets/** to files under templatesRoot with no-cache headers.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PreviewProperties previewProperties;

    public WebMvcConfig(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path templatesRoot = previewProperties.getTemplatesRoot();
        String resourceLocation = "file:" + templatesRoot.toAbsolutePath() + "/";

        registry.addResourceHandler("/assets/**")
            .addResourceLocations(resourceLocation)
            .resourceChain(false)
            .addResolver(new SecurePathResourceResolver(templatesRoot));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NoCacheInterceptor())
            .addPathPatterns("/assets/**");
    }

    /**
     * Interceptor to add no-cache headers to asset responses.
     */
    private static class NoCacheInterceptor implements HandlerInterceptor {
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response,
                               Object handler, org.springframework.web.servlet.ModelAndView modelAndView) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");
        }
    }

    /**
     * Custom resource resolver that prevents path traversal attacks.
     */
    private static class SecurePathResourceResolver extends PathResourceResolver {

        private final Path templatesRoot;

        public SecurePathResourceResolver(Path templatesRoot) {
            this.templatesRoot = templatesRoot.toAbsolutePath().normalize();
        }

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource resource = super.getResource(resourcePath, location);

            if (resource == null) {
                return null;
            }

            // Verify the resolved path is within templatesRoot
            Path resolvedPath = resource.getFile().toPath().toAbsolutePath().normalize();
            if (!resolvedPath.startsWith(templatesRoot)) {
                // Path traversal attempt detected
                return null;
            }

            return resource;
        }
    }
}
