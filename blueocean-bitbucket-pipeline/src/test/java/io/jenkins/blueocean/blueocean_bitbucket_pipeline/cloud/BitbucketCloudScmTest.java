package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudScmTest extends BbCloudWireMock {

    @Test
    public void getBitbucketScm() throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .post("/organizations/jenkins/scm/"+ BitbucketCloudScm.ID + "/" + getApiUrlParam())
                .build(Map.class);

        assertNotNull(r);
        assertEquals(BitbucketCloudScm.ID, r.get("id"));
        assertEquals(apiUrl, r.get("uri"));
        assertNull(r.get("credentialId"));
    }

    @Test
    public void getOrganizationsWithCredentialId() throws IOException, UnirestException {
        String credentialId = createCredential(BitbucketCloudScm.ID);
        List orgs = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .post("/organizations/jenkins/scm/"+BitbucketCloudScm.ID+"/organizations/"+getApiUrlParam()+"&credentialId="+credentialId)
                .build(List.class);
        assertEquals(2, orgs.size());
        assertEquals(BbCloudWireMock.USER_UUID, ((Map)orgs.get(0)).get("key"));
        assertEquals("Vivek Pandey", ((Map)orgs.get(0)).get("name"));
        assertEquals(BbCloudWireMock.TEAM_UUID, ((Map)orgs.get(1)).get("key"));
        assertEquals("Vivek's Team", ((Map)orgs.get(1)).get("name"));
    }

    @Test
    public void getOrganizationsWithoutCredentialId() throws IOException, UnirestException {
        createCredential(BitbucketCloudScm.ID);
        List orgs = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .post("/organizations/jenkins/scm/"+BitbucketCloudScm.ID+"/organizations/"+getApiUrlParam())
            .build(List.class);
        assertEquals(2, orgs.size());
        assertEquals(BbCloudWireMock.USER_UUID, ((Map)orgs.get(0)).get("key"));
        assertEquals("Vivek Pandey", ((Map)orgs.get(0)).get("name"));
        assertEquals(BbCloudWireMock.TEAM_UUID, ((Map)orgs.get(1)).get("key"));
        assertEquals("Vivek's Team", ((Map)orgs.get(1)).get("name"));
    }

    @Test
    public void getOrganizationsWithInvalidCredentialId() throws IOException, UnirestException {
        Map r = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(400)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .post("/organizations/jenkins/scm/"+ BitbucketCloudScm.ID+"/organizations/"+getApiUrlParam()+"&credentialId=foo")
            .build(Map.class);
    }

    @Test
    public void getRepositoriesWithCredentialId() throws IOException, UnirestException {
        String credentialId = createCredential(BitbucketCloudScm.ID);
        Map repoResp = new RequestBuilder(baseUrl)
                .crumb(crumb)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .post("/organizations/jenkins/scm/"+BitbucketCloudScm.ID+"/organizations/" + BbCloudWireMock.TEAM_UUID + "/repositories/"+getApiUrlParam()+"&credentialId="+credentialId)
                .build(Map.class);
        List repos = (List) ((Map)repoResp.get("repositories")).get("items");
        assertEquals("pipeline-demo-test", ((Map)repos.get(0)).get("name"));
        assertEquals("pipeline-demo-test", ((Map)repos.get(0)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(0)).get("private"));
        assertEquals("master",((Map)repos.get(0)).get("defaultBranch"));

        assertEquals(2, repos.size());
        assertEquals("emptyrepo", ((Map)repos.get(1)).get("name"));
        assertEquals("emptyrepo", ((Map)repos.get(1)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(1)).get("private"));
        assertNull(((Map)repos.get(1)).get("defaultBranch"));
    }

    @Test
    public void getRepositoriesWithoutCredentialId() throws IOException, UnirestException {
        createCredential(BitbucketCloudScm.ID);
        Map repoResp = new RequestBuilder(baseUrl)
            .crumb(crumb)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .post("/organizations/jenkins/scm/"+BitbucketCloudScm.ID+"/organizations/" + BbCloudWireMock.TEAM_UUID + "/repositories/"+getApiUrlParam())
            .build(Map.class);
        List repos = (List) ((Map)repoResp.get("repositories")).get("items");
        assertEquals("pipeline-demo-test", ((Map)repos.get(0)).get("name"));
        assertEquals("pipeline-demo-test", ((Map)repos.get(0)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(0)).get("private"));
        assertEquals("master",((Map)repos.get(0)).get("defaultBranch"));

        assertEquals(2, repos.size());
        assertEquals("emptyrepo", ((Map)repos.get(1)).get("name"));
        assertEquals("emptyrepo", ((Map)repos.get(1)).get("description"));
        assertTrue((Boolean) ((Map)repos.get(1)).get("private"));
        assertNull(((Map)repos.get(1)).get("defaultBranch"));
    }

    private String getApiUrlParam(){
        return String.format("?apiUrl=%s",apiUrl);
    }
}
