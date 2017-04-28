package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Map;

/**
 * User's permissions.
 *
 * @author Vivek Pandey
 */
@ExportedBean
public abstract class BlueUserPermission {
    private static final String ADMINISTRATOR = "administrator";
    private static final String PIPELINE = "pipeline";
    private static final String CREDENTIAL = "credential";

    /**
     * true if user has administrator privilege false otherwise
     */
    @Exported(name = ADMINISTRATOR)
    public abstract boolean isAdministration();

    /* pipeline or job permission */
    @Exported(name = PIPELINE)
    public abstract Map<String, Boolean> getPipelinePermission();

    /* credential permission */
    @Exported(name = CREDENTIAL)
    public abstract Map<String, Boolean> getCredentialPermission();
}
