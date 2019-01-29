package io.blueocean.ath.util;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;


public class GithubHelper {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Logger logger = Logger.getLogger(GithubHelper.class);

    private GitHub githubInstance;
    private GHRepository githubRepository;

    private String repositoryName;
    private String organizationOrUsername;
    private String accessToken;
    private boolean deleteRepoAfter = false;

    private boolean useRandomSuffix = false;

    public static String getRandomSuffix() {
        return new BigInteger(50, RANDOM).toString(16);
    }

    public GithubHelper() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("live.properties"));
            repositoryName = props.getProperty("github.repo");
            organizationOrUsername = props.getProperty("github.org");
            accessToken = props.getProperty("github.token");
            deleteRepoAfter = Boolean.parseBoolean(props.getProperty("github.deleteRepo", "false"));
            useRandomSuffix = Boolean.parseBoolean(props.getProperty("github.randomSuffix", "false"));
            logger.info("loaded live.properties");
        } catch (IOException e) {
            logger.error("could not load properties: " + e.getMessage());
        }

        Preconditions.checkNotNull(repositoryName, "github.repo is required");
        Preconditions.checkNotNull(organizationOrUsername, "github.org is required");
        Preconditions.checkNotNull(accessToken, "github.token is required");
    }

    public String createEmptyRepository() throws IOException {
        GitHub github = getGitHub();

        String repositoryName = !useRandomSuffix ? this.repositoryName :
            this.repositoryName + "-" + getRandomSuffix();

        try {
            String repositoryFullname = organizationOrUsername + "/" + repositoryName;
            GHRepository repositoryToDelete = github.getRepository(repositoryFullname);
            repositoryToDelete.delete();
            logger.info("Deleted repository " + repositoryFullname);
        } catch (FileNotFoundException e) {
            // fine to ignore error if the repo doesn't exist
        }

        GHCreateRepositoryBuilder builder;

        if (organizationOrUsername.equals(github.getMyself().getLogin())) {
            builder = github.createRepository(repositoryName);
        } else {
            try {
                builder = github.getOrganization(organizationOrUsername)
                    .createRepository(repositoryName);
            } catch (FileNotFoundException e) {
                logger.error("found not find organization " + organizationOrUsername);
                throw e;
            }
        }

        githubRepository = builder.autoInit(true).create();
        logger.info("Created repository " + githubRepository.getFullName());
        return repositoryName;
    }

    public void cleanupRepository() throws IOException {
        if (deleteRepoAfter && githubRepository != null) {
            githubRepository.delete();
            logger.info("Deleted repository " + githubRepository.getFullName());
            githubRepository = null;
        }
    }

    private GitHub getGitHub() throws IOException {
        if (githubInstance == null) {
            githubInstance = GitHub.connectUsingOAuth(accessToken);
            Preconditions.checkArgument(githubInstance.isCredentialValid(), "invalid GitHub access token");
            logger.info("GitHub initialized w/ valid credentials");
        }

        return githubInstance;
    }

    // getters/setters

    public String getRepositoryName() { return repositoryName;  }

    public String getActualRepositoryName() { return githubRepository.getName(); }

    public String getOrganizationOrUsername() {
        return organizationOrUsername;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public GHRepository getGithubRepository() { return githubRepository; }
}
