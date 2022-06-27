package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.jenkins.GitHubWebHook;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.commons.ServiceException;
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
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.TestExtension;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GitHubWebHook.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GithubPipelineCreateRequestTest extends GithubMockBase {

    @Override
    @After
    public void tearDown() {
        if (!perTestStubMappings.isEmpty()) {
            perTestStubMappings.forEach( mapping -> githubApi.removeStub( mapping));
            perTestStubMappings.clear();
        }
    }

    @Test
    public void createPipeline() throws UnirestException {
        PowerMockito.mockStatic(GitHubWebHook.class);
        GitHubWebHook gitHubWebHookMock = Mockito.spy(GitHubWebHook.class);
        PowerMockito.when(GitHubWebHook.get()).thenReturn(gitHubWebHookMock);
        PowerMockito.when(GitHubWebHook.getJenkinsInstance()).thenReturn(this.j.jenkins);
        String credentialId = createGithubCredential(user);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .crumb( this.crumb )
                .post("/organizations/jenkins/pipelines/")
                .data(MapsHelper.of("name", "pipeline1", "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("id", GithubScm.ID, "uri", githubApiUrl, "credentialId", credentialId,
                                "config", MapsHelper.of("repoOwner", "cloudbeers", "repository", "PR-demo"))))
                .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));

        MultiBranchProject mbp = (MultiBranchProject) j.getInstance().getItem("pipeline1");
        GitHubSCMSource source = (GitHubSCMSource) mbp.getSCMSources().get(0);
        List<SCMSourceTrait> traits = source.getTraits();

        Assert.assertNotNull(SCMTrait.find(traits, CleanAfterCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, CleanBeforeCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, LocalBranchTrait.class));

        BranchDiscoveryTrait branchDiscoveryTrait = SCMTrait.find(traits, BranchDiscoveryTrait.class);
        Assert.assertNotNull(branchDiscoveryTrait);
        Assert.assertTrue(branchDiscoveryTrait.isBuildBranch());
        Assert.assertTrue(branchDiscoveryTrait.isBuildBranchesWithPR());

        ForkPullRequestDiscoveryTrait forkPullRequestDiscoveryTrait = SCMTrait.find(traits, ForkPullRequestDiscoveryTrait.class);
        Assert.assertNotNull(forkPullRequestDiscoveryTrait);
        Assert.assertTrue(forkPullRequestDiscoveryTrait.getTrust() instanceof ForkPullRequestDiscoveryTrait.TrustPermission);
        Assert.assertEquals(1, forkPullRequestDiscoveryTrait.getStrategies().size());
        Assert.assertTrue(forkPullRequestDiscoveryTrait.getStrategies().contains(ChangeRequestCheckoutStrategy.MERGE));

        OriginPullRequestDiscoveryTrait originPullRequestDiscoveryTrait = SCMTrait.find(traits, OriginPullRequestDiscoveryTrait.class);
        Assert.assertNotNull(originPullRequestDiscoveryTrait);
        Assert.assertEquals(1, originPullRequestDiscoveryTrait.getStrategies().size());
        Assert.assertTrue(originPullRequestDiscoveryTrait.getStrategies().contains(ChangeRequestCheckoutStrategy.MERGE));
        Mockito.verify(gitHubWebHookMock, Mockito.times(1)).registerHookFor(mbp);
    }

    @Test
    public void createPipelineNoJenkinsFile() throws UnirestException, IOException {
//        AbstractMultiBranchCreateRequest.JenkinsfileCriteria criteria = Mockito.mock(AbstractMultiBranchCreateRequest.JenkinsfileCriteria.class);
//        when(criteria.isJenkinsfileFound()).thenReturn(true);
        OrganizationImpl organization = new OrganizationImpl("jenkins", j.jenkins);
        String credentialId = createGithubCredential(user);

        JSONObject config = JSONObject.fromObject(MapsHelper.of("repoOwner", "vivek", "repository", "empty1"));

        GithubPipelineCreateRequest request = new GithubPipelineCreateRequest(
                "empty1", new BlueScmConfig(GithubScm.ID, githubApiUrl, credentialId, config));

        request.create(organization, organization);
//        verify(criteria, atLeastOnce()).isJenkinsfileFound();
    }

    @Test
    public void shouldFailCreatePipelineNoJenkinsFile() throws UnirestException, IOException{
        boolean thrown = false;
        OrganizationImpl organization = new OrganizationImpl("jenkins", j.jenkins);
        String credentialId = createGithubCredential(user);

        JSONObject config = JSONObject.fromObject(MapsHelper.of("repoOwner", "vivek", "repository", "empty1"));

        GithubPipelineCreateRequest request = new GithubPipelineCreateRequest(
            "empty1/empty2", new BlueScmConfig(GithubScm.ID, githubApiUrl, credentialId, config));
        try {
            request.create(organization, organization);
        }catch (ServiceException.BadRequestException e){
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void shouldFailForAnonUserWithCredentialIdMissing() throws Exception {
        // create credential for vivek
        createGithubCredential(user);

        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(401)
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
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
                .crumb( this.crumb )
                .post("/organizations/"+getOrgName()+"/pipelines/")
                // since credentialId will default to 'github', it's okay to omit it in request
                .data(GithubTestUtils.buildRequestBody(GithubEnterpriseScm.ID, null, githubApiUrl, orgFolderName, "PR-demo"))
                .build(Map.class);
        assertNotNull(resp);
    }

    @Test
    public void shouldFindUserStoreCredential() throws IOException {
        System.setProperty(io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider.class.getName() + ".enabled", "true");
        try {
            //add username password credential to user's credential store in user domain and in USER scope
            User user = login();
            CredentialsStore store = null;
            for (CredentialsStore s : CredentialsProvider.lookupStores(user)) {
                if (s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)) {
                    store = s;
                    break;
                }
            }

            assertNotNull(store);
            store.addDomain(new Domain("github-domain",
                "GitHub Domain to store personal access token",
                Collections.<DomainSpecification>singletonList(new BlueOceanDomainSpecification())));


            Domain domain = store.getDomainByName("github-domain");
            StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                "github", "GitHub Access Token", user.getId(), "12345");
            store.addCredentials(domain, credential);

            //create another credentials with same id in system store with different description
            for (CredentialsStore s : CredentialsProvider.lookupStores(Jenkins.get())) {
                s.addCredentials(Domain.global(), new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                    "github", "System GitHub Access Token", user.getId(), "12345"));
            }

            WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "demo");
            AbstractFolderProperty prop = new BlueOceanCredentialsProvider.FolderPropertyImpl(user.getId(), credential.getId(),
                BlueOceanCredentialsProvider.createDomain("https://api.github.com"));

            mp.addProperty(prop);

            // lookup for created credential id in system store, it should resolve to previously created user store credential
            StandardCredentials c = Connector.lookupScanCredentials((Item) mp, "https://api.github.com", credential.getId());
            assertEquals("GitHub Access Token", c.getDescription());

            assertNotNull(c);
            assertTrue(c instanceof StandardUsernamePasswordCredentials);
            StandardUsernamePasswordCredentials usernamePasswordCredentials = (StandardUsernamePasswordCredentials) c;
            assertEquals(credential.getId(), usernamePasswordCredentials.getId());
            assertEquals(credential.getPassword().getPlainText(), usernamePasswordCredentials.getPassword().getPlainText());
            assertEquals(credential.getUsername(), usernamePasswordCredentials.getUsername());

            //check the domain
            Domain d = CredentialsUtils.findDomain(credential.getId(), user);
            assertNotNull(d);
            assertTrue(d.test(new BlueOceanDomainRequirement()));

            //now remove this property
            mp.getProperties().remove(prop);

            //it must resolve to system credential
            c = Connector.lookupScanCredentials((Item) mp, null, credential.getId());
            assertEquals("System GitHub Access Token", c.getDescription());
        } finally {
            System.setProperty(io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider.class.getName() + ".enabled", "false");
        }
    }

    @Test
    public void testOrgFolderIndexing() throws IOException, UnirestException {
        User user = login();
        OrganizationFolder orgFolder = j.jenkins.createProject(OrganizationFolder.class, "p");
        orgFolder.getSCMNavigators().add(new GitHubSCMNavigator("cloudbeers"));
        Map map = new RequestBuilder(baseUrl)
                .post("/organizations/jenkins/pipelines/p/runs/")
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .crumb( this.crumb )
                .data(Collections.emptyMap())
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
