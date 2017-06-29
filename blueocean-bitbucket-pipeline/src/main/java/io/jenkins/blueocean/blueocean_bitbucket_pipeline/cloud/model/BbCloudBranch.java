package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BbCloudBranch extends BbBranch{
    private final String name;
    private final String commitId;
    private final boolean defaultBranch;

    @JsonCreator
    public BbCloudBranch(@Nonnull @JsonProperty("name") String name,
                         @Nonnull @JsonProperty("target") Target target) {
        this.name = name;
        this.commitId = target.hash;
        this.defaultBranch = target.repo.getDefaultBranch().equals(name);
    }

    @Override
    public String getLatestCommit() {
        return commitId;
    }

    @Override
    public boolean isDefault() {
        return defaultBranch;
    }

    @Override
    public String getDisplayId() {
        return name;
    }

    public static class Target{
        private final String hash;
        private final BbCloudRepo repo;

        @JsonCreator
        public Target(@Nonnull @JsonProperty("hash") String hash, @Nonnull @JsonProperty("repository") BbCloudRepo repo) {
            this.hash = hash;
            this.repo = repo;
        }
    }
}
