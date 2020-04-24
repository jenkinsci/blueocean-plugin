package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrgFolder;
import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrganization;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
@PrepareForTest({OrganizationFactory.class, OrganizationFolder.class, SCMHead.HeadByItem.class,PullRequestSCMHead.class})
public class PipelineJobFiltersTest {

    @Test
    public void testFolderJobFilter(){
        BlueOrganization organization = mockOrganization();
        OrganizationFolder organizationFolder = mockOrgFolder(organization);
        assertFalse(new PipelineJobFilters.FolderJobFilter().getFilter().apply(organizationFolder));
    }

    @Test
    public void testIsPullRequest(){
        BlueOrganization organization = mockOrganization();
        OrganizationFolder organizationFolder = mockOrgFolder(organization);
        PullRequestSCMHead changeRequestSCMHead = mock(PullRequestSCMHead.class);
        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(organizationFolder)).thenReturn(changeRequestSCMHead);
        assertTrue(PipelineJobFilters.isPullRequest(organizationFolder));
        assertFalse(new PipelineJobFilters.OriginFilter().getFilter().apply(organizationFolder));
        assertTrue(new PipelineJobFilters.PullRequestFilter().getFilter().apply(organizationFolder));
    }
}
