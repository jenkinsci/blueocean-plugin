package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubOrgFolderTest extends PipelineBaseTest {
    @Test
    public void simpleOrgTest(){

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
            ImmutableMap.of("orgName", "cloudbeers",
                "scmProviderId", "github",
                "credentialId", "083f40b0-fe08-464a-95d9-21953804c6e9",
                "repos", ImmutableList.of("PR-demo")));
    }
}
