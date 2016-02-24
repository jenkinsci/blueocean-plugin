package io.jenkins.blueocean.auth.github;


import javax.inject.Inject;

import hudson.Extension;
import io.jenkins.blueocean.security.config.ApplicationConfig;

/**
 * @author Ivan Meredith
 */
@Extension
public class GithubConfig {

    @Inject
    private ApplicationConfig applicationConfig;

    public String getGithubClientId() {
        return applicationConfig.getValue("github.client.id");
    }

    public String getGithubClientSecret() {
        return applicationConfig.getValue("github.client.secret");
    }
}
