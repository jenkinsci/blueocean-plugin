package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.guice.JsonConverter;
import io.jenkins.blueocean.rest.model.hal.Link;
import io.jenkins.blueocean.rest.model.hal.Links;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * An Organization is an abstraction for Jenkins master.
 *
 * @author Vivek Pandey
 **/
public class Organization {
    /** Identifier for a Jenkins master */
    public final String id;

    /** Jenkins master version */
    public final String version;

    public final OrganizationLinks organizationLinks;

    public Organization(@Nonnull @JsonProperty("id") String id,
                        @Nonnull @JsonProperty("version") String version,
                        @Nonnull @JsonProperty("_links") OrganizationLinks organizationLinks) {
        this.id = id;
        this.version = version;
        this.organizationLinks = organizationLinks;
    }

    public static class OrganizationLinks extends Links {
        public final Link pipeline;
        public final Link user;

        public OrganizationLinks(@Nonnull @JsonProperty("self") Link self,
                                 @Nullable @JsonProperty("user") Link user,
                                 @Nullable @JsonProperty("pipeline") Link pipeline) {
            super(self);
            this.user = user;
            this.pipeline = pipeline;
        }
    }

    public static class Builder{
        private String id;
        private OrganizationLinks links;
        private String version;

        public Builder(String id) {
            this.id = id;
        }

        public Builder version(String version){
            this.version = version;
            return this;
        }

        public Builder links(OrganizationLinks links){
            this.links = links;
            return this;
        }
        public Organization build(){
            return new Organization(id,version, links);
        }
    }

    public static void main(String[] args) {
        System.out.println(JsonConverter.fromJava(
                new Organization.Builder("123")
                        .version("3.0")
                        .links(new OrganizationLinks(new Link("/organizations/123"),
                        new Link("/organizations/123/users"),
                        new Link("/organizations/123/jobs"))).build()));
    }
}
