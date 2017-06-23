package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * @author Vivek Pandey
 */
public class GithubOrgFolderTest extends GithubMockBase {

    @Test
    public void simpleOrgTest() throws IOException, UnirestException {
        String credentialId = createGithubCredential();
        String orgFolderName = "cloudbeers1";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo"), "orgName","cloudbeers"),
                                "credentialId", credentialId,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertEquals(orgFolderName, resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        TopLevelItem item = j.getInstance().getItem(orgFolderName);
        assertNotNull(item);

        Assert.assertTrue(item instanceof OrganizationFolder);


        Map r = get("/organizations/jenkins/pipelines/"+orgFolderName+"/");
        assertEquals(orgFolderName, r.get("name"));
        assertFalse((Boolean) r.get("scanAllRepos"));
    }

    @Test
    public void createGithubOrgTest() throws IOException, UnirestException {
        String credentialId = createGithubCredential();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "cloudbeers",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "credentialId", credentialId,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertEquals("cloudbeers", resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        Map repos = (Map) resp.get("repos");
        assertNotNull(repos);
        assertEquals(1, repos.size());

        Map repo = (Map) repos.get("PR-demo");
        assertNotNull(repo);
        assertTrue((Boolean) repo.get("meetsScanCriteria"));
    }

    @Test
    public void orgUpdateWithPOSTTest() throws IOException, UnirestException {
        String credentialId = createGithubCredential();
        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "credentialId", credentialId,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertEquals(orgFolderName, resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        Map repos = (Map) resp.get("repos");
        assertNotNull(repos);
        assertEquals(1, repos.size());

        Map repo = (Map) repos.get("PR-demo");
        assertNotNull(repo);
        assertTrue((Boolean) repo.get("meetsScanCriteria"));

        resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",ImmutableMap.of(
                                "credentialId", credentialId,
                                "uri", githubApiUrl))
                ))
                .build(Map.class);
        assertNotNull(resp);
    }


    @Test
    public void orgUpdateTest() throws IOException, UnirestException {
        String credentialId = createGithubCredential();
        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "credentialId", credentialId,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertEquals(orgFolderName, resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        Map repos = (Map) resp.get("repos");
        assertNotNull(repos);
        assertEquals(1, repos.size());

        Map repo = (Map) repos.get("PR-demo");
        assertNotNull(repo);
        assertTrue((Boolean) repo.get("meetsScanCriteria"));

        resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "organization","jenkins",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "credentialId", credentialId,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertNotNull(resp);
    }

    @Test
    public void shouldFindUserStoreCredential() throws IOException {
        //add username password credential to user's credential store in user domain and in USER scope
        User user = login();
        CredentialsStore store=null;
        for(CredentialsStore s: CredentialsProvider.lookupStores(user)){
            if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                store = s;
                break;
            }
        }

        assertNotNull(store);
        store.addDomain(new Domain("github-domain",
                "Github Domain to store personal access token",
                Collections.<DomainSpecification>singletonList(new BlueOceanDomainSpecification())));


        Domain domain = store.getDomainByName("github-domain");
        StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                "github", "Github Access Token", user.getId(), "12345");
        store.addCredentials(domain, credential);

        //create another credentials with same id in system store with different description
        for(CredentialsStore s: CredentialsProvider.lookupStores(Jenkins.getInstance())){
            s.addCredentials(Domain.global(), new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                    "github", "System Github Access Token", user.getId(), "12345"));
        }

        //create org folder and attach user and credential id to it
        OrganizationFolder organizationFolder = j.createProject(OrganizationFolder.class, "demo");
        AbstractFolderProperty prop = new BlueOceanCredentialsProvider.FolderPropertyImpl(user.getId(), credential.getId(),
                BlueOceanCredentialsProvider.createDomain("https://api.github.com"));

        organizationFolder.addProperty(prop);

        // lookup for created credential id in system store, it should resolve to previously created user store credential
        StandardCredentials c = Connector.lookupScanCredentials(organizationFolder, "https://api.github.com", credential.getId());
        assertEquals("Github Access Token", c.getDescription());

        assertNotNull(c);
        assertTrue(c instanceof StandardUsernamePasswordCredentials);
        StandardUsernamePasswordCredentials usernamePasswordCredentials = (StandardUsernamePasswordCredentials) c;
        assertEquals(credential.getId(), usernamePasswordCredentials.getId());
        assertEquals(credential.getPassword().getPlainText(),usernamePasswordCredentials.getPassword().getPlainText());
        assertEquals(credential.getUsername(),usernamePasswordCredentials.getUsername());

        //check the domain
        Domain d = CredentialsUtils.findDomain(credential.getId(), user);
        assertNotNull(d);
        assertTrue(d.test(new BlueOceanDomainRequirement()));

        //now remove this property
        organizationFolder.getProperties().remove(prop);

        //it must resolve to system credential
        c = Connector.lookupScanCredentials(organizationFolder, null, credential.getId());
        assertEquals("System Github Access Token", c.getDescription());
    }
}
