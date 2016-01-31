package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.guice.JsonConverter;
import io.jenkins.blueocean.rest.model.hal.Link;
import io.jenkins.blueocean.rest.model.hal.Links;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Model to represent a Jenkins Pipeline Plugin job and details associated with it
 *
 * Modeled after Pipeline API at https://github.com/cloudbees/cloudbees-workflow-plugin/tree/cloudbees-workflow-1.8/rest-api
 *
 * @author Vivek Pandey
 **/
public class Pipeline {
    public final String name;
    public final int runCounts;
    public final PipelineLinks links;

    public Pipeline(@Nonnull @JsonProperty("name") String name,
                    @JsonProperty("runCounts") int runCounts,
                    @Nullable @JsonProperty("_links") PipelineLinks links) {
        this.name = name;
        this.runCounts = runCounts;
        this.links = links;
    }

    public static class PipelineLinks extends Links{
        public final Link job;
        public final Link organization;

        public PipelineLinks(@Nonnull @JsonProperty("self") Link self,
                             @Nonnull @JsonProperty("run") Link run,
                             @Nonnull @JsonProperty("organization") Link organization) {
            super(self);
            this.job = run;
            this.organization = organization;
        }
    }

    public static void main(String[] args) {
        System.out.println(JsonConverter.fromJava(new Pipeline("test", 23,
                new PipelineLinks(new Link("/organizations/23/pieplines/12"),
                        new Link("/organizations/23/pieplines/12/jobs"),
                        new Link("/organizations/23"))
                )));
    }
}
