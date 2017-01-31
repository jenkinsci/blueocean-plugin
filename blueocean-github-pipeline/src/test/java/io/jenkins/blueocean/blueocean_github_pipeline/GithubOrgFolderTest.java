package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubOrgFolderTest extends PipelineBaseTest {
    @Test
    public void simpleOrgTest(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
            ImmutableMap.of("name", "jenkinsci",
                    "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                    "scmConfig", ImmutableMap.of("config",
                            ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 201);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));
    }

    @Test
    public void orgUpdateTest(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 201);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        put("/organizations/jenkins/pipelines/jenkinsci/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 200);

    }
}
