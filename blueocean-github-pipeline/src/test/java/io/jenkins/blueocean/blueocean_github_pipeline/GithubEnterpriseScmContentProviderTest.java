package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author cliffmeyers
 */
public class GithubEnterpriseScmContentProviderTest extends GithubMockBase {

    @Test
    public void testScmSourcePropertiesUsingNullApiUrl() throws Exception {
        testScmSourceProperties(null);
    }

    @Test
    public void testScmSourcePropertiesUsingGithubApiUrl() throws Exception {
        testScmSourceProperties(GitHubSCMSource.GITHUB_URL);
    }

    private void testScmSourceProperties(String mockedApiUrl) throws Exception {
        String credentialId = createGithubEnterpriseCredential();
        OrganizationFolder orgFolder = mockOrgFolder(credentialId);
        // ensure the enterprise provider works with enterprise org folder
        ScmContentProvider provider = new GithubEnterpriseScmContentProvider();
        assertTrue("github enterprise provider should support github enterprise org folder", provider.support(orgFolder));
        assertEquals(GithubEnterpriseScm.ID, provider.getScmId());
        assertEquals(githubApiUrl, provider.getApiUrl(orgFolder));

        // ensure the enterprise provider doesn't support cloud org folder
        credentialId = createGithubCredential(user);
        orgFolder = mockOrgFolder(credentialId);
        // unfortunately overriding the GitHub apiUrl for WireMock returns a "localhost" URL here, so we mock the call
        when(((GitHubSCMNavigator) orgFolder.getSCMNavigators().get(0)).getApiUri()).thenReturn(mockedApiUrl);
        assertFalse("github enterprise provider should not support github org folder", provider.support(orgFolder));
    }
}
