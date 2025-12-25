package com.example.thymeleafpreview.service;

import com.example.thymeleafpreview.config.PreviewProperties;
import com.example.thymeleafpreview.model.DefinitionData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for loading and merging JSON definition files.
 */
@Service
public class DefinitionService {

    private static final Logger log = LoggerFactory.getLogger(DefinitionService.class);
    private static final String GLOBAL_JSON = "global.json";

    private final PreviewProperties previewProperties;
    private final ObjectMapper objectMapper;

    public DefinitionService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Loads and merges definition data for a template.
     * Merges global.json with template-specific JSON if available.
     *
     * @param templatePath relative path to template (e.g., "pages/index.html")
     * @return merged DefinitionData
     */
    public DefinitionData loadDefinitions(String templatePath) {
        Path defsRoot = previewProperties.getDefsRoot();

        // Load global definitions
        DefinitionData globalData = loadJsonFile(defsRoot.resolve(GLOBAL_JSON));

        // Load template-specific definitions
        String jsonPath = templatePath.replace(".html", ".json");
        DefinitionData specificData = loadJsonFile(defsRoot.resolve(jsonPath));

        // Merge: specific overrides global
        return merge(globalData, specificData);
    }

    private DefinitionData loadJsonFile(Path jsonPath) {
        if (!Files.exists(jsonPath)) {
            log.debug("Definition file not found: {}", jsonPath);
            return new DefinitionData();
        }

        try {
            Map<String, Object> raw = objectMapper.readValue(
                Files.readString(jsonPath),
                new TypeReference<>() {}
            );
            return mapToDefinitionData(raw);
        } catch (IOException e) {
            log.warn("Failed to load definition file: {}", jsonPath, e);
            return new DefinitionData();
        }
    }

    @SuppressWarnings("unchecked")
    private DefinitionData mapToDefinitionData(Map<String, Object> raw) {
        DefinitionData data = new DefinitionData();

        if (raw.containsKey("css")) {
            data.setCss((List<String>) raw.get("css"));
        }

        if (raw.containsKey("js")) {
            data.setJs((List<String>) raw.get("js"));
        }

        if (raw.containsKey("fixtures")) {
            data.setFixtures((Map<String, Object>) raw.get("fixtures"));
        }

        if (raw.containsKey("dialects")) {
            data.setDialects((Map<String, Object>) raw.get("dialects"));
        }

        return data;
    }

    private DefinitionData merge(DefinitionData global, DefinitionData specific) {
        DefinitionData result = new DefinitionData();

        // CSS: specific replaces global (not appended)
        if (!specific.getCss().isEmpty()) {
            result.setCss(specific.getCss());
        } else {
            result.setCss(global.getCss());
        }

        // JS: specific replaces global (not appended)
        if (!specific.getJs().isEmpty()) {
            result.setJs(specific.getJs());
        } else {
            result.setJs(global.getJs());
        }

        // Fixtures: deep merge with specific overriding global
        Map<String, Object> mergedFixtures = new HashMap<>();
        deepMerge(mergedFixtures, global.getFixtures());
        deepMerge(mergedFixtures, specific.getFixtures());
        result.setFixtures(mergedFixtures);

        // Dialects: deep merge with specific overriding global
        Map<String, Object> mergedDialects = new HashMap<>();
        deepMerge(mergedDialects, global.getDialects());
        deepMerge(mergedDialects, specific.getDialects());
        result.setDialects(mergedDialects);

        return result;
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetValue = target.get(key);

            if (sourceValue instanceof Map && targetValue instanceof Map) {
                // Recursively merge nested maps
                Map<String, Object> mergedNested = new HashMap<>((Map<String, Object>) targetValue);
                deepMerge(mergedNested, (Map<String, Object>) sourceValue);
                target.put(key, mergedNested);
            } else {
                // Override with source value
                target.put(key, sourceValue);
            }
        }
    }
}
