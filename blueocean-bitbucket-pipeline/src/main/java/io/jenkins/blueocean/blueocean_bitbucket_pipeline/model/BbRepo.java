package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Bitbucket repository.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbRepo {
    /**
     * @return Repo slug
     */
    @JsonProperty("slug")
    public abstract String getSlug();

    /**
     * @return Repo name
     */
    @JsonProperty("name")
    public abstract String getName();

    /**
     * @return Bitbucket project/team this repo belongs to
     */
    @JsonProperty("organization")
    public abstract BbOrg getOrg();

    /**
     * @deprecated Now always true.
     */
    @Deprecated
    @JsonIgnore
    public abstract boolean isGit();

    /**
     * @return true if its private repo
     */
    @JsonProperty("private")
    public abstract boolean isPrivate();

    /**
     * Convert a {@link BbRepo} to {@link ScmRepository}.
     *
     * @param api {@link BitbucketApi}
     * @param parent {@link Reachable} parent
     * @return ScmRepository
     */
    public ScmRepository toScmRepository(@Nonnull BitbucketApi api, @Nonnull final Reachable parent){
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
