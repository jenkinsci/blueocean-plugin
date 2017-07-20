package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;

/**
 *
 * SCM server endpoint.
 *
 * @author Vivek Pandey
 */
public abstract class ScmServerEndpoint extends Resource{
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String API_URL = "apiUrl";

    /**
     * @return unique identifier of server end point
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * @return name of the SCM server endpoint
     */
    @Exported(name = NAME)
    public abstract String getName();

    /**
     * @return api URL of the SCM endpoint.
     */
    @Exported(name = API_URL)
    public abstract String getApiUrl();
}
