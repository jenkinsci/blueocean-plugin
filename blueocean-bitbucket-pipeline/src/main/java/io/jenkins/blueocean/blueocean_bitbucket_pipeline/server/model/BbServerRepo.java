package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class BbServerRepo extends BbRepo {
    private final String slug;
    private final String name;
    private final BbServerProject project;
    private final String scmId;
    private final boolean publicRepo;

    @JsonCreator
    public BbServerRepo(@NonNull @JsonProperty("slug") String slug, @NonNull @JsonProperty("name") String name,
                        @NonNull @JsonProperty("scmId") String scmId,
                        @NonNull @JsonProperty("public") Boolean publicRepo,
                        @NonNull @JsonProperty("project")BbServerProject project) {
        this.slug = slug;
        this.name = name;
        this.scmId = scmId;
        this.publicRepo = publicRepo;
        this.project = project;
    }

    @Override
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonProperty("project")
    public BbServerProject getOrg() {
        return project;
    }

    @Override
    @JsonIgnore
    public boolean isGit(){
        return scmId.equals("git");
    }

    @Override
    @JsonProperty("private")
    public boolean isPrivate() {
        return !publicRepo;
    }
}
