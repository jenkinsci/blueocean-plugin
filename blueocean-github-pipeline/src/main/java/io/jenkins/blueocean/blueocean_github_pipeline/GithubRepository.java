package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
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

    private final StandardUsernamePasswordCredentials credentials;
    private final Link self;

    public GithubRepository(GHRepository ghRepository, StandardUsernamePasswordCredentials credentials, Reachable parent) {
        this.ghRepository = ghRepository;
        this.credentials = credentials;
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
