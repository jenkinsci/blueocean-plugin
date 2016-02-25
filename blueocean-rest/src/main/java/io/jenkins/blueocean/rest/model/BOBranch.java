package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.export.Exported;

/**
 * Pipeline Branch API
 *
 * @author Vivek Pandey
 */
public abstract class BOBranch extends Resource{
    public static final String NAME="name";

    /**
     *  Branch name
     *
     *  @return gives branch name
     */
    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();
}
