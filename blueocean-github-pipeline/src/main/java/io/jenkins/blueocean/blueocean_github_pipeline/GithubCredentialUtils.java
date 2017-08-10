package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

/**
 * @author cliffmeyers
 */
public class GithubCredentialUtils {
    /**
     * Create the correct credentialId for GitHub and GitHub Enterprise.
     * @param scmId "github" or "github-enterprise"
     * @param apiUrl apiUrl, for enterprise only
     * @return credentialId
     */
    static String computeCredentialId(String scmId, String apiUrl) {
        Preconditions.checkNotNull(scmId, "scmId cannot be null");

        if (GithubScm.ID.equals(scmId)) {
            return scmId;
        }

        return GithubEnterpriseScm.ID + ":" + DigestUtils.sha256Hex(apiUrl);
    }
}
