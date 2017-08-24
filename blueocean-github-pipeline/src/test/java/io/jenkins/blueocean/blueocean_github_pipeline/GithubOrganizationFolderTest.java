package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.OrganizationFolder;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Vivek Pandey
 */
public class GithubOrganizationFolderTest extends PipelineBaseTest{
    @Test
    public void testGithubOrgFolderCreation() throws IOException {
        OrganizationFolder organizationFolder = j.jenkins.createProject(OrganizationFolder.class, "orgFolder1");
        BlueOrganization blueOrganization = mock(BlueOrganization.class);
        GithubOrganizationFolder githubOrganizationFolder = new GithubOrganizationFolder(blueOrganization, organizationFolder, new Link("abc"));
        githubOrganizationFolder.addRepo("repo1", new GithubOrganizationFolder.BlueRepositoryProperty() {
            @Override
            public boolean meetsIndexingCriteria() {
                return false;
            }
        });
        assertEquals("orgFolder1", githubOrganizationFolder.getName());
        assertEquals(1, githubOrganizationFolder.repos().size());
    }
}
