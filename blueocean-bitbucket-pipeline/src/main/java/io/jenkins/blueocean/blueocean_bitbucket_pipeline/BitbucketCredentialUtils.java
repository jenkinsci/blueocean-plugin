package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.google.common.base.Preconditions;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.BitbucketCloudScm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScm;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author cliffmeyers
 */
public class BitbucketCredentialUtils {

    public static String computeCredentialId(String scmId, String apiUrl) {
        Preconditions.checkNotNull(scmId, "scmId cannot be null");

        if (BitbucketCloudScm.ID.equals(scmId)) {
            return scmId;
        }

        return String.format("%s:%s", BitbucketServerScm.ID, DigestUtils.sha256Hex(apiUrl));
    }
}
