package io.blueocean.ath.util;

import java.util.Objects;

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
            Objects.requireNonNull(config.accessToken, "accessToken required");
            Objects.requireNonNull(config.organization, "organization required");
            Objects.requireNonNull(config.repository, "repository required");
            return config;
        }
    }
}
