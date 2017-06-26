package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;

import java.util.Collections;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public abstract class BbRepo {
    @JsonProperty("slug")
    public abstract String getSlug();

    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("organization")
    public abstract BbOrg getOrg();

    @JsonProperty("scmId")
    public abstract String getScmId();

    @JsonIgnore
    public abstract boolean isGit();

    @JsonProperty("private")
    public abstract boolean isPrivate();

    public ScmRepository toScmRepository(BitbucketApi api, final Reachable parent){
        final BbBranch defaultBranch = api.getDefaultBranch(getOrg().getKey(), getSlug());
        return new ScmRepository() {
            @Override
            public String getName() {
                return BbRepo.this.getSlug();
            }

            @Override
            public boolean isPrivate() {
                return BbRepo.this.isPrivate();
            }

            @Override
            public String getDescription() {
                return getName();
            }

            @Override
            public String getDefaultBranch() {
                if(defaultBranch != null) {
                    return defaultBranch.getDisplayId();
                }
                return null;
            }

            @Override
            public Map<String, Boolean> getPermissions() {
                return Collections.emptyMap();
            }

            @Override
            public Link getLink() {
                return parent.getLink().rel(BbRepo.this.getSlug());
            }
        };
    }

}
