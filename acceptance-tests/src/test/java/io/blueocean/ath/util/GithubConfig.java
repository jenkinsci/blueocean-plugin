package io.blueocean.ath.util;

import com.google.common.base.Preconditions;

/**
 * Config options used when creating Github-based pipeline.
 *
 * @author cliffmeyers
 */
public class GithubConfig {

    private String accessToken;
    private String organization;
    private String repository;

    public String getAccessToken() {
        return accessToken;
    }

    public String getOrganization() {
        return organization;
    }

    public String getRepository() {
        return repository;
    }


    public static class Builder {

        private final GithubConfig config = new GithubConfig();

        public Builder accessToken(String accessToken) {
            config.accessToken = accessToken;
            return this;
        }

        public Builder organization(String organization) {
            config.organization = organization;
            return this;
        }

        public Builder repository(String repository) {
            config.repository = repository;
            return this;
        }

        public GithubConfig build() {
            Preconditions.checkNotNull(config.accessToken, "accessToken required");
            Preconditions.checkNotNull(config.organization, "organization required");
            Preconditions.checkNotNull(config.repository, "repository required");
            return config;
        }
    }
}
