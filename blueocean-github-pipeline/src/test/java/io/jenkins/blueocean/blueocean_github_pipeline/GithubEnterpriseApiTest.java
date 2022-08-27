package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.credential.CredentialsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author cliffmeyers
 */
public class GithubEnterpriseApiTest extends GithubMockBase {

    @Test
    public void validateGithubToken() throws IOException, UnirestException {
        String credentialId = createGithubEnterpriseCredential();
        assertEquals(GithubCredentialUtils.computeCredentialId(null, GithubEnterpriseScm.ID, githubApiUrl), credentialId);

        //check if this credentialId is created in correct user domain
        Domain domain = CredentialsUtils.findDomain(credentialId, user);
        assertEquals("blueocean-github-enterprise-domain", domain.getName());
    }

    @Test
    public void validateGithubTokenApiUrlRequired() throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .data(MapsHelper.of("accessToken", accessToken))
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/validate")
            .build(Map.class);
        assertEquals(400, r.get("code"));
    }

    @Test
    public void fetchExistingCredentialExists() throws IOException, UnirestException {
        createGithubEnterpriseCredential();

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/?apiUrl=" + githubApiUrl)
            .build(Map.class);

        assertEquals(GithubCredentialUtils.computeCredentialId(null, GithubEnterpriseScm.ID, githubApiUrl), r.get("credentialId"));
        assertEquals(githubApiUrl, r.get("uri"));
    }

    @Test
    public void fetchExistingCredentialApiUrlRequired() throws IOException, UnirestException {
        // fetch the github-enterprise endpoint without specifying apirUrl
        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/")
            .build(Map.class);
        assertEquals(400, r.get("code"));
    }

    @Test
    public void fetchExistingCredentialNotExists() throws IOException, UnirestException {
        // create a credential using default apiUrl
        createGithubEnterpriseCredential();

        String bogusUrl = "https://foo.com";

        // look up credential for apiUrl that's invalid
        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/?apiUrl="+bogusUrl)
            .build(Map.class);

        assertNull(r.get("credentialId"));
        assertEquals(bogusUrl, r.get("uri"));
    }

    @Test
    public void fetchExistingCredentialTokenInvalid() throws UnirestException {
        createGithubEnterpriseCredential();

        addPerTestStub(
            WireMock.get(urlEqualTo("/user"))
                .willReturn(aResponse().withStatus(401))
        );

        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(428)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/?apiUrl="+githubApiUrl)
            .build(Map.class);

        assertEquals("Invalid accessToken", r.get("message").toString());
    }

    @Test
    public void fetchExistingCredentialScopesInvalid() throws UnirestException {
        createGithubEnterpriseCredential();

        addPerTestStub(
            WireMock.get(urlEqualTo("/user"))
                .willReturn(aResponse().withHeader("X-OAuth-Scopes", ""))
        );

        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(428)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/?apiUrl="+githubApiUrl)
            .build(Map.class);

        assertTrue(r.get("message").toString().contains("missing scopes"));
    }

    @Test
    public void getOrganizationsAndRepositories() throws Exception {
        String credentialId = createGithubEnterpriseCredential();

        List l = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/organizations/?credentialId="+credentialId+"&apiUrl="+githubApiUrl)
            .build(List.class);

        Assert.assertTrue(l.size() > 0);

        for (Map<String, String> org : (Iterable<Map<String, String>>) l ) {
            assertNull( org.get( "avatar" ) );
        }

        Map resp = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github-enterprise/organizations/CloudBees-community/repositories/?pageSize=10&page=1&apiUrl="+githubApiUrl)
            .build(Map.class);

        Map repos = (Map) resp.get("repositories");
        assertNotNull(repos);

        List<Map> repoItems = (List<Map>) repos.get("items");
        assertTrue(repoItems.size() > 0);
    }
}
