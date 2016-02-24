package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * Organization represents a Jenkins master in standalone case
 *
 * @author Vivek Pandey
 */
public final class Organization {
    @JsonProperty("name")
    public final String name;

    public Organization(@Nonnull @JsonProperty("name") String name) {
        this.name = name;
    }
}
