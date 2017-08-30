package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Provides metadata about the backing SCM for a BluePipeline.
 * @author cliffmeyers
 */
@ExportedBean
public abstract class BlueScmSource {

    public static final String ID = "id";
    public static final String API_URL = "apiUrl";

    /**
     * Get the identifier for the SCM.
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * Get the API URL for the SCM.
     */
    @Exported(name = API_URL)
    public abstract String getApiUrl();
}
