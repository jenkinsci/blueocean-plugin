package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerScmTest extends BitbucketWireMockBase {

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

    @Test
    public void getOrganizationsWithoutCredentialId() throws IOException, UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/?apiUrl="+apiUrl)
                .build(Map.class);

    }

    @Test
    public void getOrganizations() throws IOException, UnirestException {
        String credentialId = createCredential();
        List orgs = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .get("/organizations/jenkins/scm/"+BitbucketServerScm.ID+"/organizations/?apiUrl="+apiUrl+"&credentialId="+credentialId)
                .build(List.class);
        assertEquals(2, orgs.size());
        assertEquals("test1", ((Map)orgs.get(0)).get("name"));
        assertEquals("TEST", ((Map)orgs.get(0)).get("key"));
        assertEquals("testproject1", ((Map)orgs.get(1)).get("name"));
        assertEquals("TESTP", ((Map)orgs.get(1)).get("key"));
    }

    @Test
    public void getRepositories() throws IOException, UnirestException {
        String credentialId = createCredential();
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
}
