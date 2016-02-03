package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Pipeline Model
 *
 * Pipeline represents Jenkins Pipeline a.k.a workflow. It could also represent a Jenkins Job.
 *
 * @author Vivek Pandey
 */
public final class Pipeline {
    /** Name of the organization owning this Pipeline */
    @JsonProperty("organization")
    public final String organization;

    /** Name of the pipeline */
    @JsonProperty("name")
    public final String name;

    /** Set of branches available with this pipeline */
    @JsonProperty("branches")
    public final List<String> branches;

    public Pipeline(@Nonnull @JsonProperty("organization") String organization,
                    @Nonnull @JsonProperty("name") String name,
                    @Nonnull @JsonProperty("branches") List<String> branches) {
        this.organization = organization;
        this.name = name;
        this.branches = ImmutableList.copyOf(branches);
    }
}
