package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Lists;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.security.Permission;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationFolder;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.metadata.AvatarMetadataAction;
import org.acegisecurity.Authentication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
@PrepareForTest({OrganizationFactory.class, OrganizationFolder.class, StaplerRequest.class})
public class OrganizationFolderTest{
    @Rule
    JenkinsRule j = new JenkinsRule();

    private BlueOrganization organization;
    private OrganizationFolder orgFolder;

    @Before
    public void setup(){
        this.organization = mockOrganization();
        this.orgFolder = mockOrgFolder(organization);
    }

    @Test
    @WithoutJenkins
    public void testOrgFolderPipeline() throws IOException {
        AvatarMetadataAction avatarMetadataAction = mock(AvatarMetadataAction.class);
        when(orgFolder.getAction(AvatarMetadataAction.class)).thenReturn(avatarMetadataAction);

        BlueOrganizationFolder organizationFolder = new OrganizationFolderPipelineImpl(organization, orgFolder, organization.getLink().rel("/pipelines/")){};
        assertEquals(organizationFolder.getName(), organizationFolder.getName());
        assertEquals(organizationFolder.getDisplayName(), organizationFolder.getDisplayName());
        assertEquals(organization.getName(), organizationFolder.getOrganizationName());
        assertNotNull(organizationFolder.getIcon());
        MultiBranchProject multiBranchProject = PowerMockito.mock(MultiBranchProject.class);
        when(orgFolder.getItem("repo1")).thenReturn(multiBranchProject);
        PowerMockito.when(OrganizationFactory.getInstance().getContainingOrg((ItemGroup)multiBranchProject)).thenReturn(organization);
        PowerMockito.when(multiBranchProject.getFullName()).thenReturn("p1");
        PowerMockito.when(multiBranchProject.getName()).thenReturn("p1");
        MultiBranchPipelineContainerImpl multiBranchPipelineContainer =
                new MultiBranchPipelineContainerImpl(organization, orgFolder, organizationFolder);

        assertEquals(multiBranchProject.getName(), multiBranchPipelineContainer.get("repo1").getName());
        when(orgFolder.getItems()).thenReturn(Lists.<MultiBranchProject<?, ?>>newArrayList(multiBranchProject));
        assertNotNull(organizationFolder.getPipelineFolderNames());
    }

    @Test
    @WithoutJenkins
    public void testOrgFolderRun(){
        OrganizationFolderPipelineImpl organizationFolder = new OrganizationFolderPipelineImpl(mockOrganization(), orgFolder, new Link("/a/b/")){};

        OrganizationFolderRunImpl organizationFolderRun =  new OrganizationFolderRunImpl(organizationFolder, new Reachable() {
            @Override
            public Link getLink() {
                return new Link("/a/b/");
            }
        });

        assertEquals(orgFolder.getName(), organizationFolderRun.getPipeline());
        assertEquals(organization.getName(), organizationFolderRun.getOrganization());

        assertNotNull(organizationFolder.getRuns());
    }

    @Test
    public void testOrganizationFolderFactory() throws Exception{
        List<OrganizationFolderPipelineImpl.OrganizationFolderFactory> organizationFolderFactoryList = ExtensionList.lookup(OrganizationFolderPipelineImpl.OrganizationFolderFactory.class);
        OrganizationFolderFactoryTestImpl organizationFolderFactoryTest = ((ExtensionList<OrganizationFolderPipelineImpl.OrganizationFolderFactory>) organizationFolderFactoryList).get(OrganizationFolderFactoryTestImpl.class);
        assertNotNull(organizationFolderFactoryTest);

        OrganizationFolderPipelineImpl folderPipeline = organizationFolderFactoryTest.getFolder(orgFolder, new Reachable() {
            @Override
            public Link getLink() {
                return organization.getLink().rel("/pipelines/");
            }
        }, mockOrganization());
        assertNotNull(folderPipeline);

        assertNotNull(folderPipeline.getQueue());
        assertNotNull(folderPipeline.getQueue().iterator());

        //Make sure the user does has permissions to that folder
        PowerMockito.when(orgFolder.getACL()).thenReturn(new ACL() {
            @Override
            public boolean hasPermission(Authentication arg0, Permission arg1) {
                return true;
            }
        });

        ScmResourceImpl scmResource = new ScmResourceImpl(orgFolder, folderPipeline);
        StaplerRequest staplerRequest = PowerMockito.mock(StaplerRequest.class);
        assertEquals("hello", scmResource.getContent(staplerRequest));
    }

    @Test(expected = ServiceException.ForbiddenException.class)
    public void testOrganizationFolderFactoryNoPermissionsFolder() throws Exception {
        List<OrganizationFolderPipelineImpl.OrganizationFolderFactory> organizationFolderFactoryList = ExtensionList.lookup(OrganizationFolderPipelineImpl.OrganizationFolderFactory.class);
        OrganizationFolderFactoryTestImpl organizationFolderFactoryTest = ((ExtensionList<OrganizationFolderPipelineImpl.OrganizationFolderFactory>) organizationFolderFactoryList).get(OrganizationFolderFactoryTestImpl.class);
        assertNotNull(organizationFolderFactoryTest);

        OrganizationFolderPipelineImpl folderPipeline = organizationFolderFactoryTest.getFolder(orgFolder, new Reachable() {
            @Override
            public Link getLink() {
                return organization.getLink().rel("/pipelines/");
            }
        }, mockOrganization());
        assertNotNull(folderPipeline);

        assertNotNull(folderPipeline.getQueue());
        assertNotNull(folderPipeline.getQueue().iterator());

        //Make sure the user does not have permissions to that folder
        PowerMockito.when(orgFolder.getACL()).thenReturn(new ACL() {
            @Override
            public boolean hasPermission(Authentication arg0, Permission arg1) {
                return false;
            }
        });

        ScmResourceImpl scmResource = new ScmResourceImpl(orgFolder, folderPipeline);
        StaplerRequest staplerRequest = PowerMockito.mock(StaplerRequest.class);
        assertEquals("hello", scmResource.getContent(staplerRequest));
    }

    @TestExtension("testOrganizationFolderFactory")
    public static class ScmContentProviderTest extends ScmContentProvider {
        @Nonnull
        @Override
        public String getScmId() {
            return "TestProvider";
        }

        @Override
        public String getApiUrl(@Nonnull Item item) {
            return null;
        }

        @Override
        public Object getContent(@Nonnull StaplerRequest staplerRequest, @Nonnull Item item) {
            return "hello";
        }

        @Override
        public Object saveContent(@Nonnull StaplerRequest staplerRequest, @Nonnull Item item) {
            return null;
        }

        @Override
        public boolean support(@Nonnull Item item) {
            return item instanceof OrganizationFolder;
        }
    }

    static OrganizationFolder mockOrgFolder(BlueOrganization organization){
        OrganizationFolder orgFolder = PowerMockito.mock(OrganizationFolder.class);

        OrganizationFactory organizationFactory = mock(OrganizationFactory.class);
        PowerMockito.mockStatic(OrganizationFactory.class);
        PowerMockito.when(OrganizationFactory.getInstance()).thenReturn(organizationFactory);
        when(organizationFactory.getContainingOrg((ItemGroup) orgFolder)).thenReturn(organization);
        PowerMockito.when(orgFolder.getDisplayName()).thenReturn("vivek");
        PowerMockito.when(orgFolder.getName()).thenReturn("vivek");
        PowerMockito.when(orgFolder.getFullName()).thenReturn("vivek");

        return orgFolder;
    }

    static BlueOrganization mockOrganization(){
        BlueOrganization organization = mock(BlueOrganization.class);
        when(organization.getName()).thenReturn("jenkins");
        when(organization.getLink()).thenReturn(new Link("/blue/rest/organizations/jenkins/"));
        return organization;
    }

    @TestExtension({ "testOrganizationFolderFactory", "testOrganizationFolderFactoryNoPermissionsFolder" })
    public static class OrganizationFolderFactoryTestImpl extends OrganizationFolderPipelineImpl.OrganizationFolderFactory {
        @Override
        protected OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent, BlueOrganization organization) {
            if (folder.getName() != "orgFolder1") {
                OrganizationFolder orgFolder = mockOrgFolder(organization);
                return new OrganizationFolderPipelineImpl(organization, orgFolder, organization.getLink().rel("/pipelines/")) {
                };
            }
            return null;
        }
    }
}
