package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.BitbucketCloudScm;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import net.sf.json.JSONObject;
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

    // By default the wiremock tests will run without proxy
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy while writing and debugging tests.
    private final static boolean useProxy = !System.getProperty("test.wiremock.useProxy", "false").equals("false");

    protected User authenticatedUser;
    protected String apiUrl;


    protected abstract WireMockRule getWireMockRule();

    protected abstract String wireMockFileSystemPath();

    protected abstract String wireMockProxyUrl();

    protected abstract String getUserName();

    protected abstract String getPassword();

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

        if (useProxy) {
            bitbucketApi.stubFor(
                WireMock.get(urlMatching(".*")).atPriority(10).willReturn(aResponse()
                    .proxiedFrom(proxyUrl)));
        }

        this.apiUrl = String.format("http://localhost:%s",bitbucketApi.port());
    }

    protected String createCredential(String scmId, String apiMode, User user) throws IOException, UnirestException {

        RequestBuilder builder = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/"+ scmId+"/validate/")
                .data(MapsHelper.of("apiUrl", apiUrl,
                        "userName", getUserName(),
                        "password", getPassword()));

        Map r = builder.build(Map.class);

        assertNotNull(r);
        String credentialId = (String) r.get("credentialId");
        assertNotNull(credentialId);
        return credentialId;
    }

    protected String createCredential(String scmId, User user) throws IOException, UnirestException {
        String apiMode = "server";
        if(scmId.equals(BitbucketCloudScm.ID)){
            apiMode = "cloud";
        }
        return createCredential(scmId, apiMode, user);
    }

    protected String createCredential(String scmId) throws IOException, UnirestException {
        User user = login();
        return createCredential(scmId, user);
    }

    protected void createCredentialWithId(String jwt, String credentialId) {
        request()
            .jwtToken(jwt)
            .crumb(crumb)
            .data(MapsHelper.of("credentials", new MapsHelper.Builder()
                .put("scope", "SYSTEM")
                .put("id", credentialId)
                .put("username", "vivek")
                .put("password", "password")
                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .build()))
            .post("/organizations/jenkins/credentials/user/")
            .status(201)
            .build(JSONObject.class);
    }
}
