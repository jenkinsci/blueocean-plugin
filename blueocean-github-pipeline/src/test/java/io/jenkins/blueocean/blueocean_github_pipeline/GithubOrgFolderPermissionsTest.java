package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import net.sf.json.JSONArray;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestExtension;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GithubOrgFolderPermissionsTest extends GithubMockBase {

    @Test
    public void canCreateWhenHavePermissionsOnDefaultOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Jenkins.ADMINISTER).everywhere().to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        // refresh the JWT token otherwise all hell breaks loose.
        jwtToken = getJwtToken(j.jenkins, "vivek", "vivek");
        createCredentialWithId(jwtToken, GithubScm.ID);
        createGithubPipeline(jwtToken, true);
    }

    @Test
    public void canNotCreateWhenHaveNoPermissionOnDefaultOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ, Jenkins.READ).everywhere().to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        // refresh the JWT token otherwise all hell breaks loose.
        jwtToken = getJwtToken(j.jenkins, "vivek", "vivek");
        createCredentialWithId(jwtToken, GithubScm.ID);

        createGithubPipeline(jwtToken, false);
    }

    @Test
    public void canCreateWhenHavePermissionsOnCustomOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ, Jenkins.READ).everywhere().to(user);
        authz.grant(Item.CREATE, Item.CONFIGURE).onFolders(getOrgRoot()).to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        // refresh the JWT token otherwise all hell breaks loose.
        jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());
        createCredentialWithIdForOrg(jwtToken, GithubScm.ID, getOrgName());
        createGithubPipeline(jwtToken, true);
    }

    @Test
    public void canNotCreateWhenHaveNoPermissionOnCustomOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ, Jenkins.READ).everywhere().to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        // refresh the JWT token otherwise all hell breaks loose.
        jwtToken = getJwtToken(j.jenkins, "vivek", "vivek");
        createCredentialWithId(jwtToken, GithubScm.ID);
        createGithubPipeline(jwtToken, false);
    }

    @Test
    public void getOrganizationsOnCustomOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ, Jenkins.READ).everywhere().to("custom");
        authz.grant(Item.CREATE, Item.CONFIGURE).onFolders(getOrgRoot()).to("custom");
        j.jenkins.setAuthorizationStrategy(authz);
        String jwt = getJwtToken(j.jenkins, "custom", "custom");
        createCredentialWithIdForOrg(jwt, GithubScm.ID, getOrgName());

        JSONArray res = request()
            .crumb(crumb)
            .jwtToken(jwt)
            .status(200)
            .post("/organizations/" + getOrgName() + "/scm/github/organizations/?apiUrl=" + githubApiUrl)
            .build(JSONArray.class);

        assertEquals(6, res.size());
    }

    @Test
    public void getOrganizationsOnDefaultOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ, Jenkins.READ).everywhere().to("default");
        authz.grant(Item.CREATE, Item.CONFIGURE).onFolders(getOrgRoot()).to("default");
        j.jenkins.setAuthorizationStrategy(authz);
        String jwt = getJwtToken(j.jenkins, "default", "default");
        createCredentialWithIdForOrg(jwt, GithubScm.ID, getOrgName());

        JSONArray res = request()
            .crumb(crumb)
            .jwtToken(jwt)
            .status(200)
            .post("/organizations/" + getOrgName() + "/scm/github/organizations/?apiUrl=" + githubApiUrl)
            .build(JSONArray.class);

        assertEquals(6, res.size());
    }

    private void createGithubPipeline(String jwt, boolean shouldSucceed) {
        String pipelineName = "cloudbeers";
        Map resp = new RequestBuilder(baseUrl)
                .status(shouldSucceed ? 201 : 403)
                .jwtToken(jwt)
                .crumb( this.crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(GithubTestUtils.buildRequestBody(GithubScm.ID,null, githubApiUrl, pipelineName, "PR-demo"))
                .build(Map.class);

        TopLevelItem item = getOrgRoot().getItem(pipelineName);
        if (shouldSucceed) {
            assertEquals(pipelineName, resp.get("name"));
            assertEquals("io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl", resp.get("_class"));

            Assert.assertTrue(item instanceof WorkflowMultiBranchProject);
            Map r = get("/organizations/"+ getOrgName() + "/pipelines/"+pipelineName+"/");
            assertEquals(pipelineName, r.get("name"));
        }
        else {
            assertEquals(403, resp.get("code"));
            assertEquals("User vivek doesn't have Job create permission", resp.get("message"));
            Assert.assertNull(item);
            String r = get("/organizations/"+ getOrgName() + "/pipelines/"+pipelineName+"/", 404, String.class);
        }
    }

    private static ModifiableTopLevelItemGroup getOrgRoot() {
        return OrganizationFactory.getItemGroup(getOrgName());
    }

    @TestExtension(value={"canCreateWhenHavePermissionsOnCustomOrg","canNotCreateWhenHaveNoPermissionOnCustomOrg","getOrganizationsOnCustomOrg"})
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {

        private OrganizationImpl instance;

        public TestOrganizationFactoryImpl() throws IOException {
            Folder f = Jenkins.get().createProject(Folder.class, "CustomOrg");
            instance = new OrganizationImpl("custom", f);
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
            return Collections.singleton(instance);
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
