package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.MapsHelper;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author cliffmeyers
 */
public class GithubTestUtils {

    static Map buildRequestBody(String scmId, String credentialId, String apiUrl, String repoOwner, String repoName) {
        return MapsHelper.of(
            "name", repoOwner,
            "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
            "scmConfig", MapsHelper.of(
                "id", scmId,
                "credentialId", StringUtils.defaultIfBlank(credentialId, ""),
                "uri", apiUrl,
                "config", MapsHelper.of(
                    "repoOwner", repoOwner,
                    "repository", repoName
                )
            )
        );
    }
}
