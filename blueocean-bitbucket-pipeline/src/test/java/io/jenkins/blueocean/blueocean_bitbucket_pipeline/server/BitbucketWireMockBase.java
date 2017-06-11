package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BitbucketWireMockBase extends PipelineBaseTest{
    protected final String user="vivek";
    protected final String password="admin";
    protected User authenticatedUser;
    String apiUrl;

    @Rule
    public WireMockRule bitbucketApi = new WireMockRule(wireMockConfig().
            dynamicPort().dynamicHttpsPort()
            .usingFilesUnderClasspath("api")
    );

    @Before
    public void setup() throws Exception {
        super.setup();
        this.authenticatedUser = login();
        new File("src/test/resources/api/mappings").mkdirs();
        new File("src/test/resources/api/__files").mkdirs();
        bitbucketApi.enableRecordMappings(new SingleRootFileSource("src/test/resources/api/mappings"),
                new SingleRootFileSource("src/test/resources/api/__files"));
        bitbucketApi.stubFor(
                WireMock.get(urlMatching(".*")).atPriority(10).willReturn(aResponse()
                        .proxiedFrom("http://localhost:7990")));

        this.apiUrl = String.format("http://localhost:%s",bitbucketApi.port());
    }

    protected String createCredential() throws IOException, UnirestException {
        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/validate/")
                .data(ImmutableMap.of("apiUrl",apiUrl, "userName","vivek", "password","admin"))
                .build(Map.class);
        assertNotNull(r);
        String credentialId = (String) r.get("credentialId");
        assertNotNull(credentialId);
        return credentialId;
    }

}
