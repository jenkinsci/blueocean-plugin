package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Result;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PipelineNodeImplTest {
    @Mock
    WorkflowJob job;

    @Mock
    WorkflowRun run;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void getRun_NeverFound() throws Exception {
        try (MockedStatic<QueueUtil> queueUtilMockedStatic = Mockito.mockStatic(QueueUtil.class)) {
            Mockito.when(QueueUtil.getRun(job, 1)).thenReturn(null);

            WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
            assertNull(workflowRun);

            Mockito.verify(QueueUtil.class, VerificationModeFactory.atLeastOnce());
            QueueUtil.getRun(job, 1); // need to call again to handle verify
        }
    }

    @Test
    public void getRun_FirstFound() throws Exception {
        try (MockedStatic<QueueUtil> queueUtilMockedStatic = Mockito.mockStatic(QueueUtil.class)) {
            Mockito.when(QueueUtil.getRun(job, 1)).thenReturn(run);

            WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
            assertEquals(workflowRun, run);

            Mockito.verify(QueueUtil.class, VerificationModeFactory.times(1));
            QueueUtil.getRun(job, 1); // need to call again to handle verify
        }
    }

    @Test
    public void getRun_EventuallyFound() throws Exception {
        try (MockedStatic<QueueUtil> queueUtilMockedStatic = Mockito.mockStatic(QueueUtil.class)) {
            Mockito.when(QueueUtil.getRun(job, 1)).thenReturn(null).thenReturn(null).thenReturn(null).thenReturn(run);

            WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
            assertEquals(workflowRun, run);

            Mockito.verify(QueueUtil.class, VerificationModeFactory.times(4));
            QueueUtil.getRun(job, 1); // need to call again to handle verify
        }
    }

    @Issue("JENKINS-54250")
    @Test
    public void getRun_isRestartable() throws Exception {
        String jobName = "JENKINS-54250-restartFromStageEnabled";
        WorkflowRun run = createAndRunJob(jobName, jobName + ".jenkinsfile");

        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        FlowNodeWrapper node = nodes.stream().filter(flowNode -> "foo".equals(flowNode.getDisplayName())).findFirst().get();

        Reachable parent = Mockito.mock(Reachable.class);
        Link link = Mockito.mock(Link.class);
        Mockito.when(parent.getLink()).thenReturn(link);
        Mockito.when(link.rel(node.getId())).thenReturn(link);

        PipelineNodeImpl underTest = new PipelineNodeImpl(node, parent, run);
        assertNotNull(underTest);
        assertTrue(underTest.isRestartable());
    }

    @Issue("JENKINS-54250")
    @Test
    public void getRun_isNotRestartable() throws Exception {
        String jobName = "JENKINS-54250-restartFromStageDisabled";
        WorkflowRun run = createAndRunJob(jobName, jobName + ".jenkinsfile");

        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        FlowNodeWrapper node = nodes.stream().filter(flowNode -> "foo".equals(flowNode.getDisplayName())).findFirst().get();

        Reachable parent = Mockito.mock(Reachable.class);
        Link link = Mockito.mock(Link.class);
        Mockito.when(parent.getLink()).thenReturn(link);
        Mockito.when(link.rel(node.getId())).thenReturn(link);

        PipelineNodeImpl underTest = new PipelineNodeImpl(node, parent, run);
        assertNotNull(underTest);
        assertFalse(underTest.isRestartable());
    }

    private WorkflowRun createAndRunJob(String jobName, String jenkinsFileName) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class, jobName);

        URL resource = getClass().getResource(jenkinsFileName);
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
        return job.getLastBuild();
    }

}
