package io.jenkins.blueocean.blueocean_github_pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;


public class GithubOrgFolderPermissionsTest extends GithubMockBase {

    @Test
    public void canCreateWhenHavePermissionsOnDefaultOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Jenkins.ADMINISTER).everywhere().to(user.getId());
        j.jenkins.setAuthorizationStrategy(authz);
        createGithubOrgFolder();
    }

    @Test
    public void canNotCreateWhenHaveNoPermissionOnDefaultOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ).everywhere().to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        createGithubOrgFolder();
    }

    @Test
    public void canCreateWhenHavePermissionsOnCustomOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ).everywhere().to(user);
        authz.grant(Item.CREATE, Item.CONFIGURE).onFolders(getOrgRoot());
        j.jenkins.setAuthorizationStrategy(authz);
        createGithubOrgFolder();
    }

    @Test
    public void canNotCreateWhenHaveNoPermissionOnCustomOrg() throws Exception {
        MockAuthorizationStrategy authz = new MockAuthorizationStrategy();
        authz.grant(Item.READ).everywhere().to(user);
        j.jenkins.setAuthorizationStrategy(authz);
        createGithubOrgFolder();
    }

    private void createGithubOrgFolder() throws Exception {
        String credentialId = createGithubCredential(user);
        String orgFolderName = "cloudbeers1";
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
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

        TopLevelItem item = getOrgRoot().getItem(orgFolderName);
        assertNotNull(item);

        Assert.assertTrue(item instanceof OrganizationFolder);


        Map r = get("/organizations/"+ getOrgName() + "/pipelines/"+orgFolderName+"/");
        assertEquals(orgFolderName, r.get("name"));
        assertFalse((Boolean) r.get("scanAllRepos"));
    }

    private String createGithubCredential(User user) throws UnirestException {
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", "12345"))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put("/organizations/" + getOrgName() + "/scm/github/validate/")
                .build(Map.class);

        assertEquals("github", r.get("credentialId"));
        return "github";
    }

    private static String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    private static ModifiableTopLevelItemGroup getOrgRoot() {
        return OrganizationFactory.getItemGroup(getOrgName());
    }

    @TestExtension(value={"canCreateWhenHavePermissionsOnCustomOrg","canNotCreateWhenHaveNoPermissionOnCustomOrg"})
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {

        private OrganizationImpl instance;

        public TestOrganizationFactoryImpl() throws IOException {
            System.out.println("HELLO THERE");
            Folder f = Jenkins.getInstance().createProject(Folder.class, "CustomOrg");
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

//    private static class DummyOrganisationAutorizationStrategy extends AuthorizationStrategy {
//
//        @Override
//        public ACL getACL(AbstractItem item) {
//            if (isWithinOrg(item)) {
//                return new SidACL() {
//                    
//                    @Override
//                    protected Boolean hasPermission(Sid p, Permission permission) {
//                        if (toString(p).equals(ACL.SYSTEM_USERNAME)) {
//                            return Boolean.TRUE;
//                        }
//                        // full control here!
//                        if (toString(p).equals("vivek")) {
//                            return Boolean.TRUE;
//                        }
//                        return Boolean.FALSE;
//                    }
//                };
//            }
//            return super.getACL(item);
//        }
//        
//        @Override
//        public ACL getRootACL() {
//            return new SidACL() {
//                
//                @Override
//                protected Boolean hasPermission(Sid p, Permission permission) {
//                    if (toString(p).equals(ACL.SYSTEM_USERNAME)) {
//                        return Boolean.TRUE;
//                    }
//                    if (toString(p).equals("vivek")) {
//                        if (permission.equals(Item.READ)) {
//                            return Boolean.TRUE;
//                        }
//                    }
//                    return Boolean.FALSE;
//                }
//            };
//        }
//
//        @Override
//        public Collection<String> getGroups() {
//            return Collections.emptySet();
//        }
//
//        boolean isWithinOrg(Item item) {
//            Object orgRoot = getOrgRoot();
//            while (item != null && ! (item instanceof AbstractCIBase)) {
//                if (orgRoot.equals(item)) {
//                    return true;
//                }
//                ItemGroup<? extends Item> parent = item.getParent();
//                if (parent instanceof Item) {
//                    item = (Item)parent;
//                }
//                else {
//                    item = null;
//                }
//            }
//            return false;
//        }
//    }
}
