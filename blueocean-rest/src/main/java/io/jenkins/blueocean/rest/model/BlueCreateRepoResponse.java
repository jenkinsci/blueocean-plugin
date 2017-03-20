package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Response object of pipeline creation
 *
 * @author Vivek Pandey
 */
@ExportedBean
public abstract class BlueCreateRepoResponse {
    @Exported
    public abstract String getStatus();

    @Navigable
    public abstract Object getLog();
}
