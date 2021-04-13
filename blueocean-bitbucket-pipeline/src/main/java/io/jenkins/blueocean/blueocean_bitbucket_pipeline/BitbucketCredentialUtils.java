package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.BitbucketCloudScm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScm;
import io.jenkins.blueocean.commons.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author cliffmeyers
 */
public class BitbucketCredentialUtils {

    /**
     * Create proper credentialId for Bitbucket Cloud and Server.
     * If null/empty credentialId value is passed in, will compute proper default value.
     *
     * @param credentialId
     * @param scmId "bitbucket-cloud" or "bitbucket-server"
     * @param apiUrl apiUrl, for server only
     * @return
     */
    public static String computeCredentialId(String credentialId, String scmId, String apiUrl) {
        if (StringUtils.isNotBlank(credentialId)) {
            return credentialId;
        }

        if (BitbucketCloudScm.ID.equals(scmId)) {
            return scmId;
        }

        return String.format( "%s:%s", BitbucketServerScm.ID, DigestUtils.sha256Hex(apiUrl));
    }
}
