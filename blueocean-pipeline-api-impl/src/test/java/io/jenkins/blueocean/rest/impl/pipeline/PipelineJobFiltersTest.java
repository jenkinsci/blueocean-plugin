package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrgFolder;
import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrganization;
import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.organizationFactoryMockedStatic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class PipelineJobFiltersTest {

    private MockedStatic<SCMHead.HeadByItem> mockedStatic;

    @After
    public void cleanup() {
        if(organizationFactoryMockedStatic!=null) {
            organizationFactoryMockedStatic.close();
            organizationFactoryMockedStatic = null;
        }
        if(mockedStatic != null) {
            mockedStatic.close();
        }
    }
    @Test
    public void testFolderJobFilter(){
        BlueOrganization organization = mockOrganization();
        OrganizationFolder organizationFolder = mockOrgFolder(organization);
        assertFalse(new PipelineJobFilters.FolderJobFilter().getFilter().test(organizationFolder));
    }

    @Test
    public void testIsPullRequest(){
        BlueOrganization organization = mockOrganization();
        OrganizationFolder organizationFolder = mockOrgFolder(organization);
        PullRequestSCMHead changeRequestSCMHead = Mockito.mock(PullRequestSCMHead.class);
        mockedStatic = Mockito.mockStatic(SCMHead.HeadByItem.class);
        Mockito.when(SCMHead.HeadByItem.findHead(organizationFolder)).thenReturn(changeRequestSCMHead);
        assertTrue(PipelineJobFilters.isPullRequest(organizationFolder));
        assertFalse(new PipelineJobFilters.OriginFilter().getFilter().test(organizationFolder));
        assertTrue(new PipelineJobFilters.PullRequestFilter().getFilter().test(organizationFolder));
    }
}
