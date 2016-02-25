package io.jenkins.blueocean.rest.sandbox;

import org.kohsuke.stapler.export.Exported;

/**
 * Pipeline Branch API
 *
 * @author Vivek Pandey
 */
public abstract class BOBranch extends Resource{

    /** Branch name */
    @Exported
    public abstract String getName();
}
