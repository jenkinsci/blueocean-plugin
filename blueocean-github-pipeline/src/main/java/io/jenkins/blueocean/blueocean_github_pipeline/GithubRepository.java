package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.export.Exported;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubRepository extends ScmRepository {
    private final GHRepository ghRepository;

    private final String accessToken;
    private final Link self;

    public GithubRepository(GHRepository ghRepository, String accessToken, Reachable parent) {
        this.ghRepository = ghRepository;
        this.accessToken = accessToken;
        this.self = parent.getLink().rel(ghRepository.getName());
    }

    @Override
    public String getName() {
        return ghRepository.getName();
    }

    /** Full name of github repository */
    @Exported
    public String getFullName() {
        return ghRepository.getFullName();
    }

    @Override
    public boolean isPrivate() {
        return ghRepository.isPrivate();
    }

    @Override
    public String getDescription() {
        return ghRepository.getDescription();
    }

    @Override
    public String getDefaultBranch() {
        return ghRepository.getDefaultBranch();
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return ImmutableMap.of("admin",ghRepository.hasAdminAccess(),
                "push", ghRepository.hasPushAccess(),
                "pull", ghRepository.hasPullAccess());
    }

    @Override
    public Link getLink() {
        return self;
    }
}
