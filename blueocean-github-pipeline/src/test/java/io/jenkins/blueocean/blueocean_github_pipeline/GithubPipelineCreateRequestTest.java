package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.Item;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.OrganizationFolder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GithubPipelineCreateRequestTest {

    // Regression test for JENKINS-43471
    @Test
    public void testCleanupRemovesItemWhenCreatingNewItem() throws Exception {
        Item item = mock(Item.class);
        try {
            GithubPipelineCreateRequest.cleanupOnError(new Exception("foo"), "My Pipeline", item, true);
        } catch (Exception e) {
            assertTrue(e instanceof UnexpectedErrorException);
        }
        verify(item, times(1)).delete();
    }

    // Regression test for JENKINS-43471
    @Test
    public void testCleanupRemovesItemWhenNotCreatingNewItem() throws Exception {
        Item item = mock(Item.class);
        try {
            GithubPipelineCreateRequest.cleanupOnError(new Exception("foo"), "My Pipeline", item, false);
        } catch (Exception e) {
            assertTrue(e instanceof UnexpectedErrorException);
        }
        verify(item, never()).delete();
    }

    // https://github.com/i386/branch-api-plugin/pull/1
    @Test
    public void testUpdateNavigatorAutoDiscover() throws Exception {
        GithubPipelineCreateRequest req = new GithubPipelineCreateRequest("My Cool Organization", new BlueScmConfig("http://github.example/api", "my-secret", new JSONObject()));
        OrganizationFolder folder = mock(OrganizationFolder.class);
        List<String> repos = ImmutableList.of("bob", "dylan");
        req.updateNavigator(repos, folder, "my-secret", false);

        GitHubSCMNavigator navigator = folder.getNavigators().get(GitHubSCMNavigator.class);
        assertNotNull(navigator);
    }
}
