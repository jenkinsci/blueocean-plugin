package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;
import org.kohsuke.github.GHMyself;

/**
 * Github user login such as 'vivek' is an org as well but github API does not expose it via organization API.
 *
 * This class expose github user login name as an organization
 *
 * @author Vivek Pandey
 */
public class GithubUserOrganization  extends AbstractGithubOrganization{

    private final GithubScm parent;
    private final GHMyself user;
    private final Link self;
    private final StandardUsernamePasswordCredentials credential;


    public GithubUserOrganization(GHMyself user, StandardUsernamePasswordCredentials credentials, GithubScm parent) {
        this.parent = parent;
        this.user = user;
        this.self = parent.getLink().rel("organizations").rel(user.getLogin());
        this.credential = credentials;
    }

    @Override
    public String getAvatar() {
        return getAvatarWithSize(user.getAvatarUrl());
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public String getName() {
        return user.getLogin();
    }

    @Override
    public ScmRepositoryContainer getRepositories() {
        return new GithubRespositoryContainer(parent, String.format("%s/user", parent.getUri()), getName(), "owner" ,credential,this);
    }
}
