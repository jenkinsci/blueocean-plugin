package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.ProxyConfiguration;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.credential.CredentialsUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GithubApiTest extends GithubMockBase {
    @Test
    public void validateGithubToken() throws IOException, UnirestException {
        //check credentialId of this SCM, should be null
        createGithubCredential(user);
        //check if this credentialId is created in correct user domain
        Domain domain = CredentialsUtils.findDomain("github", user);
        assertEquals("blueocean-github-domain", domain.getName());

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        Map r = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/github/?apiUrl="+githubApiUrl)
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
        assertEquals("github", r.get("id"));

        //now try validating again, it should return the same credentialId
        r = new RequestBuilder(baseUrl)
                .data(MapsHelper.of("accessToken", accessToken))
                .status(200)
                .crumb(crumb)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/github/validate/?apiUrl="+githubApiUrl)
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
    }

    @Test
    public void fetchExistingCredentialTokenInvalid() throws UnirestException {
        createGithubCredential(user);

        addPerTestStub(
            WireMock.get(urlEqualTo("/user"))
                .willReturn(aResponse().withStatus(401))
        );

        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(428)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github/?apiUrl="+githubApiUrl)
            .build(Map.class);

        assertEquals("Invalid accessToken", r.get("message").toString());
    }

    @Test
    public void fetchExistingCredentialScopesInvalid() throws UnirestException {
        createGithubCredential(user);

        addPerTestStub(
            WireMock.get(urlEqualTo("/user"))
                .willReturn(aResponse().withHeader("X-OAuth-Scopes", ""))
        );

        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(428)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/scm/github/?apiUrl="+githubApiUrl)
            .build(Map.class);

        assertTrue(r.get("message").toString().contains("missing scopes"));
    }

    @Test
    public void getOrganizationsAndRepositories() throws Exception {
        String credentialId = createGithubCredential(user);

        List l = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/github/organizations/?credentialId=" + credentialId+"&apiUrl="+githubApiUrl)
                .build(List.class);

        Assert.assertTrue(l.size() > 0);

        Map resp = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/?pageSize=10&page=1&apiUrl="+githubApiUrl)
                .build(Map.class);

        Map repos = (Map) resp.get("repositories");
        assertNotNull(repos);

        List<Map> repoItems = (List<Map>) repos.get("items");
        assertTrue(repoItems.size() > 0);

        resp = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/RunMyProcess-task/?apiUrl="+githubApiUrl)
                .build(Map.class);

        assertEquals("RunMyProcess-task", resp.get("name"));
    }

    @Test
    public void proxyTest() throws IOException {
        HttpURLConnection connection = HttpRequest.get(j.getURL().toString()).connect();
        Assert.assertFalse(connection.usingProxy());

        URL wiremockProxy = new URL(githubApiUrl);
        j.jenkins.proxy = new ProxyConfiguration(wiremockProxy.getHost(), wiremockProxy.getPort());

        connection = HttpRequest.get(j.getURL().toString()).connect();
        Assert.assertTrue(connection.usingProxy());
    }
}
