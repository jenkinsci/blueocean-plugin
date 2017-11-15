package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BbCloudPipelineCreateRequestTest extends BbCloudWireMock {
    @Test
    public void createPipeline() throws UnirestException, IOException {
        createCredential(BitbucketCloudScm.ID);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "pipeline1", "$class", "io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("id", BitbucketCloudScm.ID, "uri", apiUrl,
                                "config", ImmutableMap.of("repoOwner", "vivektestteam", "repository", "pipeline-demo-test"))))
                .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));
    }

}
