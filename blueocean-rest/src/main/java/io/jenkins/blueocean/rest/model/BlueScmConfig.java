package io.jenkins.blueocean.rest.model;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

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

    @DataBoundConstructor
    public BlueScmConfig(String uri, String credentialId, JSONObject config) {
        this.uri = uri;
        this.credentialId = credentialId;
        this.config = config;
    }



    /**
     * Gives URI of SCM
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gives credential id
     */
    public String getCredentialId() {
        return credentialId;
    }


    /**
     * Gives map of configuration item specific to this pipeline creation
     */
    public @Nonnull Map<String, Object> getConfig() {
        return config;
    }

}
