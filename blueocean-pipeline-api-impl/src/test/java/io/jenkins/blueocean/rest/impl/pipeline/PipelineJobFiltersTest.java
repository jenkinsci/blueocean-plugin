package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.junit.Test;
import org.mockito.Mockito;

import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrgFolder;
import static io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderTest.mockOrganization;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class PipelineJobFiltersTest {

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
        Mockito.mockStatic(SCMHead.HeadByItem.class);
        Mockito.when(SCMHead.HeadByItem.findHead(organizationFolder)).thenReturn(changeRequestSCMHead);
        assertTrue(PipelineJobFilters.isPullRequest(organizationFolder));
        assertFalse(new PipelineJobFilters.OriginFilter().getFilter().test(organizationFolder));
        assertTrue(new PipelineJobFilters.PullRequestFilter().getFilter().test(organizationFolder));
    }
}
