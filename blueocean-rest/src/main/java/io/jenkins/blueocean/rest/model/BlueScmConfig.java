package io.jenkins.blueocean.rest.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SCM configuration
 *
 * @author Vivek Pandey
 */
public class BlueScmConfig {
    private String uri;
    private String credentialId;

    private Map<String, Object> config = new LinkedHashMap<>();

    /**
     * Gives URI of SCM
     */
    public @Nullable String getUri() {
        return uri;
    }

    public void setUri(@Nullable String uri) {
        this.uri = uri;
    }

    /**
     * Gives credential id
     */
    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    /**
     * Gives map of configuration item specific to this pipeline creation
     */
    public @Nonnull Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(@Nonnull Map<String, Object> config) {
        this.config.putAll(config);;
    }
}
