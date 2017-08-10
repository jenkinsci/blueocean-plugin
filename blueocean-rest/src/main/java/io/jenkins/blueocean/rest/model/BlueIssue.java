package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents an issue or ticket
 */
@ExportedBean
public abstract class BlueIssue {

    public static final String ID = "id";
    public static final String URL = "url";

    /**
     * @return issue identifier
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * @return issue URL
     */
    @Exported(name = URL)
    public abstract String getURL();
}
