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
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import jenkins.branch.OrganizationFolder;
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
public class GithubOrgFolderTest extends PipelineBaseTest {
    @Test
    public void simpleOrgTest() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));
    }

    @Test
    public void orgUpdateTest() throws IOException, UnirestException {
        login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .put("/organizations/jenkins/pipelines/jenkinsci/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);
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
        store.addDomain(new Domain("github-domain",
                "Github Domain to store personal access token",
                Collections.<DomainSpecification>emptyList()
        ));
        Domain domain = store.getDomainByName("github-domain");
        StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                "github", "Github Access Token", user.getId(), "12345");
        store.addCredentials(domain, credential);


        //create org folder and attach user and credential id to it
        OrganizationFolder organizationFolder = j.createProject(OrganizationFolder.class, "demo");
        AbstractFolderProperty prop = new BlueOceanCredentialsProvider.FolderPropertyImpl(user.getId(), credential.getId(), "github-domain"

        );
        organizationFolder.addProperty(prop);

        // lookup for created credential id in system store, it should resolve to previously created user store credential
        StandardCredentials c = Connector.lookupScanCredentials(organizationFolder, null, credential.getId());
        assertNotNull(c);
        assertTrue(c instanceof StandardUsernamePasswordCredentials);
        StandardUsernamePasswordCredentials usernamePasswordCredentials = (StandardUsernamePasswordCredentials) c;
        assertEquals(credential.getId(), usernamePasswordCredentials.getId());
        assertEquals(credential.getPassword().getPlainText(),usernamePasswordCredentials.getPassword().getPlainText());
        assertEquals(credential.getUsername(),usernamePasswordCredentials.getUsername());

        //now remove this property
        organizationFolder.getProperties().remove(prop);

        //it must not be found
        c = Connector.lookupScanCredentials(organizationFolder, null, credential.getId());
        assertNull(c);
    }
}
