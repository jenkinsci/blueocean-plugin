package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BbCloudRepo extends BbRepo {
    private final String slug;
    private final String name;
    private final String scm;
    private final boolean isPrivate;
    private final BbCloudTeam team;
    private final String defaultBranch;

    public BbCloudRepo(@Nonnull @JsonProperty("slug")String slug,
                       @Nonnull @JsonProperty("name") String name,
                       @Nonnull @JsonProperty("scm") String scm,
                       @JsonProperty("is_private") boolean isPrivate,
                       @Nullable @JsonProperty("mainbranch") Map<String,String> mainBranch,
                       @Nonnull @JsonProperty("owner") BbCloudTeam team) {
        this.slug = slug;
        this.name = name;
        this.team = team;
        this.scm = scm;
        this.isPrivate = isPrivate;
        this.defaultBranch = mainBranch == null ? null : mainBranch.get("name");
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BbOrg getOrg() {
        return team;
    }

    @Override
    public boolean isGit() {
        return scm.equalsIgnoreCase("git");
    }

    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    public String getDefaultBranch(){
        return defaultBranch;
    }
}
