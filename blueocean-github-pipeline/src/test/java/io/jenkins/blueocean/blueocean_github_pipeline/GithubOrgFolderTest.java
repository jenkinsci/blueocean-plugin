package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.branch.OrganizationFolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Vivek Pandey
 */
public class GithubOrgFolderTest extends PipelineBaseTest {
    @Test
    public void simpleOrgTest() throws InterruptedException {
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
            ImmutableMap.of("name", "jenkinsci",
                    "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                    "scmConfig", ImmutableMap.of("config",
                            ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 201);

        assertEquals("jenkinsci", resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));


        TopLevelItem item = j.getInstance().getItem("jenkinsci");
        Assert.assertNotNull(item);

        Assert.assertTrue(item instanceof OrganizationFolder);


        Map r = get("/organizations/jenkins/pipelines/jenkinsci/");
        assertEquals("jenkinsci", r.get("name"));
        assertFalse((Boolean) r.get("scanAllRepos"));
    }

    @Test
    public void orgUpdateTest(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 201);

        assertEquals("jenkinsci", resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        put("/organizations/jenkins/pipelines/jenkinsci/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ), 200);

    }
}
