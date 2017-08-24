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
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineCreateRequestTest extends GithubMockBase {
    @Test
    public void createPipeline() throws UnirestException, IOException {
        String credentialId = createGithubCredential(user);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "pipeline1", "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("id", GithubScm.ID, "uri", githubApiUrl, "credentialId", credentialId,
                                "config", ImmutableMap.of("repoOwner", "cloudbeers", "repository", "PR-demo"))))
                .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));
    }

    @Test
    public void createPipelineNoJenkinsFile() throws UnirestException, IOException {
//        AbstractMultiBranchCreateRequest.JenkinsfileCriteria criteria = Mockito.mock(AbstractMultiBranchCreateRequest.JenkinsfileCriteria.class);
//        when(criteria.isJekinsfileFound()).thenReturn(true);
        OrganizationImpl organization = new OrganizationImpl("jenkins", j.jenkins);
        String credentialId = createGithubCredential(user);

        JSONObject config = JSONObject.fromObject(ImmutableMap.of("repoOwner", "vivek", "repository", "empty1"));

        GithubPipelineCreateRequest request = new GithubPipelineCreateRequest(
                "empty1", new BlueScmConfig(GithubScm.ID, githubApiUrl, credentialId, config));

        request.create(organization, organization);
//        verify(criteria, atLeastOnce()).isJekinsfileFound();
    }

    @Test
    public void shouldFailForAnonUserWithCredentialIdMissing() throws Exception {
        // create credential for vivek
        createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(401)
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID,null, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldFailForAnonUserWithCredentialIdSent() throws Exception {
        // create credential for vivek
        String credentialId = createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(401)
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, credentialId, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldFailForAuthedUserWithoutCredentialCreatedAndCredentialIdMissing() throws Exception {
        // create credential for vivek
        createGithubCredential(user);
        // switch to bob
        User user = login();

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID,null, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldFailForAuthedUserWithoutCredentialCreatedAndCredentialIdSent() throws Exception {
        // create credential for default vivek user
        String credentialId = createGithubCredential(user);
        // switch to bob
        User user = login();

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, credentialId, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldSucceedForAuthedUserWithCredentialCreatedAndCredentialIdMissing() throws Exception {
        // switch to bob and create a credential
        User user = login();
        createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                // since credentialId will default to 'github', it's okay to omit it in request
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, null, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldSucceedForAuthedUserWithCredentialCreatedAndCredentialIdSent() throws Exception {
        // switch to bob and create a credential
        User user = login();
        String credentialId = createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, credentialId, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldFailForAuthedUserWithCredentialCreatedAndBogusCredentialIdSent() throws Exception {
        // switch to bob and create a credential
        User user = login();
        createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, "bogus-cred", githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldSucceedForAuthedUserWithCredentialCreatedAndCredentialIdMissingEnterprise() throws Exception {
        // switch to bob and create a credential
        User user = login();
        createGithubEnterpriseCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/"+getOrgName()+"/pipelines/")
                // since credentialId will default to 'github', it's okay to omit it in request
                .data(GithubTestUtils.buildRequestBody(GithubEnterpriseScm.ID, null, githubApiUrl, orgFolderName, "PR-demo"))
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

        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "demo");
        AbstractFolderProperty prop = new BlueOceanCredentialsProvider.FolderPropertyImpl(user.getId(), credential.getId(),
                BlueOceanCredentialsProvider.createDomain("https://api.github.com"));

        mp.addProperty(prop);

        // lookup for created credential id in system store, it should resolve to previously created user store credential
        StandardCredentials c = Connector.lookupScanCredentials((Item)mp, "https://api.github.com", credential.getId());
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
        mp.getProperties().remove(prop);

        //it must resolve to system credential
        c = Connector.lookupScanCredentials((Item)mp, null, credential.getId());
        assertEquals("System Github Access Token", c.getDescription());
    }

    @Test
    public void testOrgFolderIndexing() throws IOException, UnirestException {
        User user = login();
        OrganizationFolder orgFolder = j.jenkins.createProject(OrganizationFolder.class, "p");
        orgFolder.getSCMNavigators().add(new GitHubSCMNavigator("cloudbeers"));
        Map map = new RequestBuilder(baseUrl)
                .post("/organizations/jenkins/pipelines/p/runs/")
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .data(ImmutableMap.of())
                .status(200)
                .build(Map.class);

        assertNotNull(map);
    }

    public static class TestOrganizationFolder extends OrganizationFolderPipelineImpl {

        public TestOrganizationFolder(BlueOrganization organization, OrganizationFolder folder, Link parent) {
            super(organization, folder, parent);
        }
    }

    @TestExtension("testOrgFolderIndexing")
    public static class OrganizationFolderFactoryImpl extends OrganizationFolderPipelineImpl.OrganizationFolderFactory {
        @Override
        protected OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent, BlueOrganization organization) {
            if (folder.getName().equals("p")){
                return new TestOrganizationFolder(organization, folder, parent.getLink());
            }
            return null;
        }
    }
}
