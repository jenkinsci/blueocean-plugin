package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.Item;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import org.junit.Test;

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
}
