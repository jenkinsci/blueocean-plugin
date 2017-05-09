package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class GithubApiTest extends PipelineBaseTest {

    private static final String accessToken = System.getProperty("GITHUB_ACCESS_TOKEN");

    @BeforeClass
    public static void checkAccessToken() {
        Assume.assumeTrue("GITHUB_ACCESS_TOKEN jvm property not set, ignoring test", accessToken != null);
    }

    @Test
    public void validateGithubToken() throws IOException, UnirestException {
        User user = login();

        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);
        Assert.assertNull(r.get("credentialId"));
        assertEquals("github", r.get("id"));

        r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));

        //check if this credentialId is created in correct user domain
        Domain domain = CredentialsUtils.findDomain("github", user);
        assertEquals("blueocean-github-domain", domain.getName());

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
        assertEquals("github", r.get("id"));

        //now try validating again, it should return the same credentialId
        r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
    }

    @Test
    public void validateGithubEnterpriseToken() throws IOException, UnirestException {
        User user = login();

        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github-enterprise/")
                .build(Map.class);
        Assert.assertNull(r.get("credentialId"));
        assertEquals("github-enterprise", r.get("id"));
    }

    @Test
    public void getOrganizationsAndRepositories() throws Exception {
        User user = login();

        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", accessToken))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);


        assertEquals("github", r.get("credentialId"));
        String credentialId = (String) r.get("credentialId");


        List l = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/?credentialId=" + credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(List.class);

        Assert.assertTrue(l.size() > 0);


        Map resp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/?credentialId=" + credentialId + "&pageSize=10&pageNumber=3")
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);

        Map repos = (Map) resp.get("repositories");
        assertNotNull(repos);

        List<Map> repoItems = (List<Map>) repos.get("items");
        assertTrue(repoItems.size() > 0);

        resp = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/game-of-life/?credentialId=" + credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId + "sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);

        assertEquals("game-of-life", resp.get("name"));
    }
}
