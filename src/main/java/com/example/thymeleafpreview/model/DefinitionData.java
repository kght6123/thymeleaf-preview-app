package com.example.thymeleafpreview.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merged JSON data passed to templates during rendering.
 */
public class DefinitionData {

    private List<String> css = new ArrayList<>();
    private List<String> js = new ArrayList<>();
    private Map<String, Object> fixtures = new HashMap<>();

    public DefinitionData() {
    }

    public List<String> getCss() {
        return css;
    }

    public void setCss(List<String> css) {
        this.css = css != null ? css : new ArrayList<>();
    }

    public List<String> getJs() {
        return js;
    }

    public void setJs(List<String> js) {
        this.js = js != null ? js : new ArrayList<>();
    }

    public Map<String, Object> getFixtures() {
        return fixtures;
    }

    public void setFixtures(Map<String, Object> fixtures) {
        this.fixtures = fixtures != null ? fixtures : new HashMap<>();
    }
}
