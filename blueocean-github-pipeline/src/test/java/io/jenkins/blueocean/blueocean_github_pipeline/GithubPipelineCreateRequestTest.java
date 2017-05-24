package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import hudson.model.Item;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.pubsub.PubsubBus;
import org.jenkinsci.plugins.pubsub.SimpleMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GithubPipelineCreateRequest.class,OrganizationFolder.class, PubsubBus.class})
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

    @Test
    public  void test_sendOrganizationScanCompleteEvent() throws Exception {
        PubsubBus pubsubBus = Mockito.mock(PubsubBus.class);
        PowerMockito.mockStatic(PubsubBus.class);
        Mockito.when(PubsubBus.getBus()).thenReturn(pubsubBus);
        Mockito.doNothing().when(pubsubBus).publish(Mockito.any(SimpleMessage.class));

        Item item = Mockito.mock(Item.class);
        OrganizationFolder organizationFolder = PowerMockito.mock(OrganizationFolder.class);
        MultiBranchProject mbp = Mockito.mock(MultiBranchProject.class);
        Mockito.when(mbp.getName()).thenReturn("PR-demo");

        Mockito.when(organizationFolder.getName()).thenReturn("cloudbeers");
        Mockito.when(organizationFolder.getItem("cloudbeers")).thenReturn(mbp);

        PowerMockito.spy(GithubPipelineCreateRequest.class);

        JSONObject config = new JSONObject();
        config.put("repos", ImmutableList.of("PR-demo"));
        config.put("orgName", ImmutableList.of("cloudbeers"));
        GithubPipelineCreateRequest pipelineCreateRequest = new GithubPipelineCreateRequest("cloudbeers", "jenkins",
                new BlueScmConfig(null, "12345", config));

        Whitebox.invokeMethod(pipelineCreateRequest, "_sendOrganizationScanCompleteEvent", item, organizationFolder);
        Mockito.verify(pubsubBus, Mockito.atLeastOnce()).publish(Mockito.any(SimpleMessage.class));
    }

    @Test
    public  void testSendOrganizationScanCompleteEvent() throws Exception {
        PubsubBus pubsubBus = Mockito.mock(PubsubBus.class);
        PowerMockito.mockStatic(PubsubBus.class);
        Mockito.when(PubsubBus.getBus()).thenReturn(pubsubBus);
        Mockito.doNothing().when(pubsubBus).publish(Mockito.any(SimpleMessage.class));

        Item item = Mockito.mock(Item.class);
        OrganizationFolder organizationFolder = PowerMockito.mock(OrganizationFolder.class);
        MultiBranchProject mbp = Mockito.mock(MultiBranchProject.class);
        Mockito.when(mbp.getName()).thenReturn("PR-demo");

        Mockito.when(organizationFolder.getName()).thenReturn("cloudbeers");
        Mockito.when(organizationFolder.getItem("cloudbeers")).thenReturn(mbp);

        PowerMockito.spy(GithubPipelineCreateRequest.class);

        JSONObject config = new JSONObject();
        config.put("repos", ImmutableList.of("PR-demo"));
        config.put("orgName", ImmutableList.of("cloudbeers"));
        GithubPipelineCreateRequest pipelineCreateRequest = new GithubPipelineCreateRequest("cloudbeers", "jenkins",
                new BlueScmConfig(null, "12345", config));

        Whitebox.invokeMethod(pipelineCreateRequest, "sendOrganizationScanCompleteEvent", item, organizationFolder);
    }


    @Test
    public  void testSendMultibranchIndexingCompleteEvent() throws Exception {
        PubsubBus pubsubBus = Mockito.mock(PubsubBus.class);
        PowerMockito.mockStatic(PubsubBus.class);
        Mockito.when(PubsubBus.getBus()).thenReturn(pubsubBus);
        Mockito.doNothing().when(pubsubBus).publish(Mockito.any(SimpleMessage.class));

        Item item = Mockito.mock(Item.class);
        OrganizationFolder organizationFolder = PowerMockito.mock(OrganizationFolder.class);
        MultiBranchProject mbp = Mockito.mock(MultiBranchProject.class);
        Mockito.when(mbp.getName()).thenReturn("PR-demo");

        Mockito.when(organizationFolder.getName()).thenReturn("cloudbeers");
        Mockito.when(organizationFolder.getItem("cloudbeers")).thenReturn(mbp);

        PowerMockito.spy(GithubPipelineCreateRequest.class);

        JSONObject config = new JSONObject();
        config.put("repos", ImmutableList.of("PR-demo"));
        config.put("orgName", ImmutableList.of("cloudbeers"));
        GithubPipelineCreateRequest pipelineCreateRequest = new GithubPipelineCreateRequest("cloudbeers", "jenkins",
                new BlueScmConfig(null, "12345", config));

        Whitebox.invokeMethod(pipelineCreateRequest, "_sendMultibranchIndexingCompleteEvent", item, organizationFolder, "cloudbeers",5);
        Mockito.verify(pubsubBus, Mockito.atLeastOnce()).publish(Mockito.any(SimpleMessage.class));
    }

}
