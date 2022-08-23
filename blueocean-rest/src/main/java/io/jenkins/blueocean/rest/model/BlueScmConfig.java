package io.jenkins.blueocean.rest.model;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SCM configuration
 *
 * @author Vivek Pandey
 */
public class BlueScmConfig {
    private String id;
    private String uri;
    private String credentialId;

    private Map<String, Object> config = new LinkedHashMap<>();

    @DataBoundConstructor
    public BlueScmConfig(String id, String uri, String credentialId, JSONObject config) {
        this.id = id;
        this.uri = uri;
        this.credentialId = credentialId;
        this.config = config;
    }

    /**
     * Gives ID of SCM
     */
    public @NonNull String getId() {
        return id;
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
    public @NonNull Map<String, Object> getConfig() {
        return config;
    }

}
