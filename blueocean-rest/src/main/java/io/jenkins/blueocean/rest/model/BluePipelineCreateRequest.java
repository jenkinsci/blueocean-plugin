package io.jenkins.blueocean.rest.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pipeline create request
 *
 * @author Vivek Pandey
 */
public class BluePipelineCreateRequest {
    /** Name of the new plugin */
    private String name;

    /** Mode of the new plugin to be created. It must be the id of factory
     * implementation that can create inctance of this pipeline
     */
    private String creatorId;

    /**
     * Map of configuration item specific to this pipeline creation
     */
    private Map<String, Object> config = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config.putAll(config);
    }
}
