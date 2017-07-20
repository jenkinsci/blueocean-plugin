package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author cliffmeyers
 */
public class GithubEnterpriseApiTest extends GithubMockBase {

    @Test
    public void validateGithubToken() throws IOException, UnirestException {
        String credentialId = createGithubEnterpriseCredential();
        assertEquals("github-enterprise:" + getGithubApiUrlEncoded(), credentialId);

        //check if this credentialId is created in correct user domain
        Domain domain = CredentialsUtils.findDomain(credentialId, user);
        assertEquals("blueocean-github-enterprise-domain", domain.getName());
    }

    @Test
    public void validateGithubToken_apiUrlRequired() throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .data(ImmutableMap.of("accessToken", accessToken))
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .put("/organizations/jenkins/scm/github-enterprise/validate")
            .build(Map.class);
        assertEquals(400, r.get("code"));
    }

    @Test
    public void fetchExistingCredential_exists() throws IOException, UnirestException {
        createGithubEnterpriseCredential();

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        Map r = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .get("/organizations/jenkins/scm/github-enterprise/?apiUrl=" + githubApiUrl)
            .build(Map.class);

        assertEquals("github-enterprise:" + getGithubApiUrlEncoded(), r.get("credentialId"));
        assertEquals(githubApiUrl, r.get("uri"));
    }

    @Test
    public void fetchExistingCredential_apiUrlRequired() throws IOException, UnirestException {
        // fetch the github-enterprise endpoint without specifiying apirUrl
        Map r = new RequestBuilder(baseUrl)
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .get("/organizations/jenkins/scm/github-enterprise/")
            .build(Map.class);
        assertEquals(400, r.get("code"));
    }

    @Test
    public void fetchExistingCredential_notExists() throws IOException, UnirestException {
        // create a credential using default apiUrl
        createGithubEnterpriseCredential();

        String bogusUrl = "https://foo.com";

        // look up credential for apiUrl that's invalid
        Map r = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .get("/organizations/jenkins/scm/github-enterprise/?apiUrl="+bogusUrl)
            .build(Map.class);

        assertNull(r.get("credentialId"));
        assertEquals(bogusUrl, r.get("uri"));
    }

    @Test
    public void getOrganizationsAndRepositories() throws Exception {
        String credentialId = createGithubEnterpriseCredential();

        List l = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .get("/organizations/jenkins/scm/github-enterprise/organizations/?credentialId=" + credentialId+"&apiUrl="+githubApiUrl)
            .build(List.class);

        Assert.assertTrue(l.size() > 0);

        Iterator<Map<String,String>> it = l.iterator();
        while(it.hasNext()) {
            Map<String, String> org = it.next();
            assertNull(org.get("avatar"));
        }
    }
}
