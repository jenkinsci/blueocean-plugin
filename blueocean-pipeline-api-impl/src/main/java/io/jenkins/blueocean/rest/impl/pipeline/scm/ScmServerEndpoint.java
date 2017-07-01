package io.jenkins.blueocean.rest.impl.pipeline.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * SCM endpoint.
 *
 * To be used for SCM severs which can be installed as separate HTTP endpoints
 *
 * @author Vivek Pandey
 */
@ExportedBean
public abstract class ScmServerEndpoint{
    public static final String NAME = "name";
    public static final String API_URL = "apiUrl";

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
