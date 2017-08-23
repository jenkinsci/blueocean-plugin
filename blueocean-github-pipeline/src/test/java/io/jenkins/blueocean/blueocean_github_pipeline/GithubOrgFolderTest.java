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

import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.User;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Vivek Pandey
 */
@RunWith(Parameterized.class)
public class GithubOrgFolderTest extends GithubMockBase {

    @Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GithubOrgFolderTest(String blueOrganisation) {
        System.out.println("setting org root to: " + blueOrganisation);
        TestOrganizationFactoryImpl.orgRoot = blueOrganisation;
    }

    @Test
    public void simpleOrgTest() throws IOException, UnirestException {
        createGithubCredential(user);
        String orgFolderName = "cloudbeers1";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo"), "orgName","cloudbeers"),
                                "id", GithubScm.ID,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertEquals(orgFolderName, resp.get("name"));
        assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        TopLevelItem item = getOrgRoot().getItem(orgFolderName);
        assertNotNull(item);

        Assert.assertTrue(item instanceof OrganizationFolder);


        Map r = get("/organizations/"+ getOrgName() + "/pipelines/"+orgFolderName+"/");
        assertEquals(orgFolderName, r.get("name"));
        assertFalse((Boolean) r.get("scanAllRepos"));
    }

    @Test
    public void createGithubOrgTest() throws IOException, UnirestException {
        createGithubCredential(user);
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of("name", "cloudbeers",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "id", GithubScm.ID,
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
        String credentialId = createGithubCredential(user);
        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "id", GithubScm.ID,
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
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID, credentialId, githubApiUrl, orgFolderName, null))
                .build(Map.class);
        assertNotNull(resp);
    }


    @Test
    public void orgUpdateTest() throws IOException, UnirestException {
        String credentialId = createGithubCredential(user);
        String orgFolderName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "id", GithubScm.ID,
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
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of("name", orgFolderName,
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "organization","jenkins",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("PR-demo")),
                                "id", GithubScm.ID,
                                "uri", githubApiUrl)
                ))
                .build(Map.class);

        assertNotNull(resp);
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

        //create org folder and attach user and credential id to it
        OrganizationFolder organizationFolder = (OrganizationFolder) getOrgRoot().createProject((TopLevelItemDescriptor)j.jenkins.getDescriptor(OrganizationFolder.class), "demo", false);
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

    private ModifiableTopLevelItemGroup getOrgRoot() {
        return OrganizationFactory.getItemGroup(getOrgName());
    }


    @TestExtension
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {

        public static String orgRoot;

        private OrganizationImpl instance;

        public TestOrganizationFactoryImpl() {
            System.out.println("TestOrganizationFactoryImpl org root is: " + orgRoot);
            setOrgRoot(orgRoot);
        }

        private void setOrgRoot(String root) {
            if (root != null) {
                try {
                    MockFolder itemGroup = Jenkins.getInstance().createProject(MockFolder.class, root);
                    instance = new OrganizationImpl(root, itemGroup);
                } catch (IOException e) {
                    throw new RuntimeException("Test setup failed!", e);
                }

            }
            else {
                instance = new OrganizationImpl("jenkins", Jenkins.getInstance());
            }
        }

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    System.out.println("" + name + " Instance returned " + instance);
                    return instance;
                }
            }
            System.out.println("" + name + " no instance found");
            return null;
        }

        @Override
        public Collection<BlueOrganization> list() {
            return Collections.singleton((BlueOrganization) instance);
        }

        @Override
        public OrganizationImpl of(ItemGroup group) {
            if (group == instance.getGroup()) {
                return instance;
            }
            return null;
        }
    }

}
