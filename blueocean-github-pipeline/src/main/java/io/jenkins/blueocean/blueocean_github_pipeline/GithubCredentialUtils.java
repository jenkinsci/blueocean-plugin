package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author cliffmeyers
 */
public class GithubCredentialUtils {
    /**
     * Create proper credentialId for GitHub and GitHub Enterprise.
     * If null/empty credentialId value is passed in, will compute proper default value.
     *
     * @param scmId "github" or "github-enterprise"
     * @param apiUrl apiUrl, for enterprise only
     * @return credentialId
     */
    static String computeCredentialId(String credentialId, String scmId, String apiUrl) {
        if(StringUtils.isNotBlank(credentialId)) {
            return credentialId;
        }

        if (GithubScm.ID.equals(scmId)) {
            return scmId;
        }

        return GithubEnterpriseScm.ID + ":" + DigestUtils.sha256Hex(apiUrl);
    }
}
