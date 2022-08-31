package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.RestartDeclarativePipelineAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PipelineNodeImplTest {
    @Mock
    WorkflowJob job;

    @Mock
    WorkflowRun run;

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
    @Test public void getRun_isRestartable() throws Exception {
        FlowNode flowNode = Mockito.mock(FlowNode.class);
        NodeRunStatus nodeRunStatus = Mockito.mock(NodeRunStatus.class);
        TimingInfo timingInfo = Mockito.mock(TimingInfo.class);
        StageAction stageAction = Mockito.mock(StageAction.class);

        Mockito.when(flowNode.getAction(StageAction.class)).thenReturn(stageAction);
        Mockito.when(flowNode.getDisplayName()).thenReturn("DummyName");

        FlowNodeWrapper node = Mockito.mock(FlowNodeWrapper.class, Mockito.withSettings().useConstructor(flowNode, nodeRunStatus, timingInfo, this.run));

        Reachable parent = Mockito.mock(Reachable.class);
        Link link = Mockito.mock(Link.class);

        Mockito.when(node.getStatus()).thenReturn(nodeRunStatus);
        Mockito.when(node.getTiming()).thenReturn(timingInfo);
        Mockito.when(timingInfo.getTotalDurationMillis()).thenReturn(1000L);
        Mockito.when(parent.getLink()).thenReturn(link);
        Mockito.when(node.getId()).thenReturn("dummyId");
        Mockito.when(link.rel(node.getId())).thenReturn(link);
        Mockito.when(node.getDisplayName()).thenReturn("DummyName");

        PipelineNodeImpl underTest = Mockito.mock(PipelineNodeImpl.class, Mockito.withSettings().useConstructor(node, parent, this.run));
        assertNotNull(underTest);

        RestartDeclarativePipelineAction restartableAction = Mockito.mock(RestartDeclarativePipelineAction.class);
        List<String> restartableStages = Mockito.mock(List.class);
        Mockito.when(this.run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(restartableAction);
        Mockito.when(restartableAction.isRestartEnabled()).thenReturn(true);
        Mockito.when(restartableAction.getRestartableStages()).thenReturn(restartableStages);
        Mockito.when(underTest.getDisplayName()).thenReturn("DummyName");
        Mockito.when(restartableStages.contains(underTest.getDisplayName())).thenReturn(true);
        Mockito.when(underTest.getStateObj()).thenReturn(BlueRun.BlueRunState.FINISHED);
        Mockito.when(underTest.isRestartable()).thenCallRealMethod();

        assertTrue(underTest.isRestartable());
    }

    @Issue("JENKINS-54250")
    @Test public void getRun_isNotRestartable() throws Exception {
        FlowNode flowNode = Mockito.mock(FlowNode.class);
        NodeRunStatus nodeRunStatus = Mockito.mock(NodeRunStatus.class);
        TimingInfo timingInfo = Mockito.mock(TimingInfo.class);
        StageAction stageAction = Mockito.mock(StageAction.class);

        Mockito.when(flowNode.getAction(StageAction.class)).thenReturn(stageAction);
        Mockito.when(flowNode.getDisplayName()).thenReturn("DummyName");

        FlowNodeWrapper node = Mockito.mock(FlowNodeWrapper.class, Mockito.withSettings().useConstructor(flowNode, nodeRunStatus, timingInfo, this.run));

        Reachable parent = Mockito.mock(Reachable.class);
        Link link = Mockito.mock(Link.class);

        Mockito.when(node.getStatus()).thenReturn(nodeRunStatus);
        Mockito.when(node.getTiming()).thenReturn(timingInfo);
        Mockito.when(timingInfo.getTotalDurationMillis()).thenReturn(1000L);
        Mockito.when(parent.getLink()).thenReturn(link);
        Mockito.when(node.getId()).thenReturn("dummyId");
        Mockito.when(link.rel(node.getId())).thenReturn(link);
        Mockito.when(node.getDisplayName()).thenReturn("DummyName");

        PipelineNodeImpl underTest = Mockito.mock(PipelineNodeImpl.class, Mockito.withSettings().useConstructor(node, parent, this.run));
        assertNotNull(underTest);

        RestartDeclarativePipelineAction restartableAction = Mockito.mock(RestartDeclarativePipelineAction.class);
        Mockito.when(this.run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(restartableAction);
        Mockito.when(restartableAction.isRestartEnabled()).thenReturn(false);
        assertFalse(underTest.isRestartable());
    }
}
