package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author cliffmeyers
 */
public class GithubTestUtils {

    static Map buildRequestBody(String scmId, String credentialId, String apiUrl, String orgFolderName, String repoName) {
        String[] repoNames = repoName != null ? new String[] { repoName } : new String[]{};

        return ImmutableMap.of(
            "name", orgFolderName,
            "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
            "scmConfig", ImmutableMap.of(
                "id", scmId,
                "credentialId", StringUtils.defaultIfBlank(credentialId, ""),
                "uri", apiUrl,
                "config", ImmutableMap.of(
                    "orgName", orgFolderName,
                    "repos", ImmutableList.copyOf(repoNames)
                )
            )
        );
    }
}
