package io.jenkins.blueocean.rest.sandbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.WebMethod;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Organization extends Resource {
    @JsonProperty
    protected final String name;

    private boolean populated;

    public Organization(String name) {
        this.name = name;
    }

    public abstract String getIconUrl();

    public abstract PipelineContainer getPipelines();

}

