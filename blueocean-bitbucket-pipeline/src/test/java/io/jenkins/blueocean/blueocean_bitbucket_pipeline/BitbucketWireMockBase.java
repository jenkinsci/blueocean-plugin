package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public abstract class BitbucketWireMockBase extends PipelineBaseTest{
    protected final String user="vivek";
    protected final String password="admin";
    protected User authenticatedUser;
    protected String apiUrl;

    protected abstract WireMockRule getWireMockRule();

    protected abstract String wireMockFileSystemPath();

    protected abstract String wireMockProxyUrl();

    @Before
    public void setup() throws Exception {
        super.setup();
        this.authenticatedUser = login();

        WireMockRule bitbucketApi = getWireMockRule();

        String files = wireMockFileSystemPath()+"__files";
        String mappings = wireMockFileSystemPath()+"mappings";
        String proxyUrl = wireMockProxyUrl();

        new File(mappings).mkdirs();
        new File(files).mkdirs();
        bitbucketApi.enableRecordMappings(new SingleRootFileSource(mappings),
                new SingleRootFileSource(files));
        bitbucketApi.stubFor(
                WireMock.get(urlMatching(".*")).atPriority(10).willReturn(aResponse()
                        .proxiedFrom(proxyUrl)));

        this.apiUrl = String.format("http://localhost:%s",bitbucketApi.port());
    }

    protected String createCredential(String scmId) throws IOException, UnirestException {
        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/"+ scmId+"/validate/")
                .data(ImmutableMap.of("apiUrl",apiUrl, "userName","vivek", "password","admin"))
                .build(Map.class);
        assertNotNull(r);
        String credentialId = (String) r.get("credentialId");
        assertNotNull(credentialId);
        return credentialId;
    }

}
