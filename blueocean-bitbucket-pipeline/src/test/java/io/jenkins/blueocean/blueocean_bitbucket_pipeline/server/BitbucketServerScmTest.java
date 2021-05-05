package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.util.HttpRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerScmTest extends BbServerWireMock {

    String token;

    @Before
    public void setUp() throws Exception {
        super.setup();
        token = getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId());
    }

    @Test
    public void getBitbucketScmWithoutApiUrlParam() throws IOException, UnirestException {
        new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/")
                .build(Map.class);
    }

    @Test
    public void getBitbucketScm() throws IOException, UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/?apiUrl="+apiUrl)
                .build(Map.class);

        assertNotNull(r);
        assertEquals(BitbucketServerScm.ID, r.get("id"));
        assertEquals(apiUrl, r.get("uri"));
        assertNull(r.get("credentialId"));
    }


    /**
     * Checks different server urls and consistency of generated credentialId
     */
    @Test
    public void getScmNormalizedUrlTest() throws IOException, UnirestException {
        String credentialId = createCredential(BitbucketServerScm.ID);

        String apiUrl = this.apiUrl;
        String normalizedUrl = BitbucketEndpointConfiguration.normalizeServerUrl(apiUrl);
        String expectedCredId = BitbucketServerScm.ID+":"+ DigestUtils.sha256Hex(normalizedUrl);
        assertEquals(credentialId, expectedCredId);

        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+String.format("?apiUrl=%s",apiUrl))
                .build(Map.class);

        assertEquals(normalizedUrl, r.get("uri"));
        assertEquals(expectedCredId, r.get("credentialId"));
        String apiUrlWithSlash = this.apiUrl+"/";
        assertNotEquals(apiUrl, apiUrlWithSlash);
        String normalizedUrlWithSlash = BitbucketEndpointConfiguration.normalizeServerUrl(apiUrl);
        assertEquals(normalizedUrl, normalizedUrlWithSlash);

        String expectedCredIdWithSlash = BitbucketServerScm.ID+":"+ DigestUtils.sha256Hex(normalizedUrlWithSlash);
        assertEquals(expectedCredId, expectedCredIdWithSlash);

        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+String.format("?apiUrl=%s",apiUrl))
                .build(Map.class);

        assertEquals(expectedCredId, r.get("credentialId"));
        assertEquals(normalizedUrl, r.get("uri"));
    }

    @Test
    public void getOrganizationsWithCredentialId() throws IOException, UnirestException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        List orgs = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/?apiUrl="+apiUrl+"&credentialId="+credentialId)
            .build(List.class);
        assertEquals(3, orgs.size());
        assertEquals("Vivek Pandey", ((Map)orgs.get(0)).get("name"));
        assertEquals("~vivek", ((Map)orgs.get(0)).get("key"));
        assertEquals("test1", ((Map)orgs.get(1)).get("name"));
        assertEquals("TEST", ((Map)orgs.get(1)).get("key"));
        assertEquals("testproject1", ((Map)orgs.get(2)).get("name"));
        assertEquals("TESTP", ((Map)orgs.get(2)).get("key"));
    }

    @Test
    public void getOrganizationsWithoutCredentialId() throws IOException, UnirestException {
        createCredential(BitbucketServerScm.ID);
        List orgs = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/?apiUrl="+apiUrl)
                .build(List.class);
        assertEquals(3, orgs.size());
        assertEquals("Vivek Pandey", ((Map)orgs.get(0)).get("name"));
        assertEquals("~vivek", ((Map)orgs.get(0)).get("key"));
        assertEquals("test1", ((Map)orgs.get(1)).get("name"));
        assertEquals("TEST", ((Map)orgs.get(1)).get("key"));
        assertEquals("testproject1", ((Map)orgs.get(2)).get("name"));
        assertEquals("TESTP", ((Map)orgs.get(2)).get("key"));
    }

    @Test
    public void getOrganizationsWithInvalidCredentialId() throws IOException, UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/?apiUrl="+apiUrl+"&credentialId=foo")
            .build(Map.class);
    }

    @Test
    public void getRepositoriesWithCredentialId() throws IOException, UnirestException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        Map repoResp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/TESTP/repositories/?apiUrl="+apiUrl+"&credentialId="+credentialId)
                .build(Map.class);
        List repos = (List) ((Map)repoResp.get("repositories")).get("items");
        assertEquals(2, repos.size());
        assertEquals("empty-repo-test", ((Map)repos.get(0)).get("name"));
        assertEquals("empty-repo-test", ((Map)repos.get(0)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(0)).get("private"));
        assertNull(((Map)repos.get(0)).get("defaultBranch"));

        assertEquals("pipeline-demo-test", ((Map)repos.get(1)).get("name"));
        assertEquals("pipeline-demo-test", ((Map)repos.get(1)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(1)).get("private"));
        assertEquals("master",((Map)repos.get(1)).get("defaultBranch"));
    }

    @Test
    public void getRepositoriesWithoutCredentialId() throws IOException, UnirestException {
        createCredential(BitbucketServerScm.ID);
        Map repoResp = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/TESTP/repositories/?apiUrl="+apiUrl)
            .build(Map.class);
        List repos = (List) ((Map)repoResp.get("repositories")).get("items");
        assertEquals(2, repos.size());
        assertEquals("empty-repo-test", ((Map)repos.get(0)).get("name"));
        assertEquals("empty-repo-test", ((Map)repos.get(0)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(0)).get("private"));
        assertNull(((Map)repos.get(0)).get("defaultBranch"));

        assertEquals("pipeline-demo-test", ((Map)repos.get(1)).get("name"));
        assertEquals("pipeline-demo-test", ((Map)repos.get(1)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(1)).get("private"));
        assertEquals("master",((Map)repos.get(1)).get("defaultBranch"));
    }

    /**
     * Test that fetching the bitbucket-server scm produces a 401 with proper error message when the saved BB creds are invalid.
     */
    @Test
    public void getScmWithRevokedCredential() throws IOException, UnirestException {
        createCredential(BitbucketServerScm.ID);

        // downstream call to BB should return a 401 when checking the credential
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/rest/api/1.0/users/"+getUserName()))
            .willReturn(aResponse().withStatus(401)));

        Map result = httpRequest()
            .Get("/organizations/{org}/scm/{scmid}/?apiUrl={apiurl}")
            .urlPart("org", "jenkins")
            .urlPart("scmid", BitbucketServerScm.ID)
            .urlPart("apiurl", apiUrl)
            .status(401)
            .as(Map.class);

        assertEquals(401, result.get("code"));
        assertTrue("must contain message about existing credential", result.get("message").toString().contains("Existing credential failed"));
    }

    private HttpRequest httpRequest() {
        return new HttpRequest(baseUrl)
            .header("Authorization", "Bearer "+token);
    }
}
