package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.apache.http.nio.protocol.PipeliningClientExchangeHandler;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.RestartDeclarativePipelineAction;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.SkippedStageReason;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WorkflowRun.class, WorkflowJob.class, QueueUtil.class })
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class PipelineNodeImplTest {
    @Mock
    WorkflowJob job;

    @Mock
    WorkflowRun run;

    @Test
    public void getRun_NeverFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(null);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertNull(workflowRun);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.atLeastOnce());
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Test
    public void getRun_FirstFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(run);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertEquals(workflowRun, run);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.times(1));
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Test
    public void getRun_EventuallyFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(null).thenReturn(null).thenReturn(null).thenReturn(run);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertEquals(workflowRun, run);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.times(4));
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Issue("JENKINS-54250")
    @Test public void getRun_isRestartable_withDefaults() throws Exception {
        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        RestartDeclarativePipelineAction action = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        List<String> restartableStages = PowerMockito.mock(List.class);

        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, this.run);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(action);
        PowerMockito.when(this.run.getParent()).thenReturn(job);
        PowerMockito.when(action.isRestartEnabled()).thenReturn(true);
        PowerMockito.when(action.getRestartableStages()).thenReturn(restartableStages);
        PowerMockito.when(restartableStages.contains(underTest.getDisplayName())).thenReturn(true);
        PowerMockito.when(underTest.getStateObj()).thenReturn(BlueRun.BlueRunState.FINISHED);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        assertTrue(underTest.isRestartable());
    }

    @Issue("JENKINS-54250")
    @Test public void getRun_isRestartable() throws Exception {
        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        RestartDeclarativePipelineAction action = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        List<String> restartableStages = PowerMockito.mock(List.class);
        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, this.run);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(action);
        PowerMockito.when(this.run.getParent()).thenReturn(job);
        PowerMockito.when(action.isRestartEnabled()).thenReturn(true);
        PowerMockito.when(action.getRestartableStages()).thenReturn(restartableStages);
        PowerMockito.when(restartableStages.contains(underTest.getDisplayName())).thenReturn(true);
        PowerMockito.when(underTest.getStateObj()).thenReturn(BlueRun.BlueRunState.FINISHED);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        assertTrue(underTest.isRestartable());
    }

    @Issue("JENKINS-54250")
    @Test public void getRun_isNotRestartable() throws Exception {
        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        RestartDeclarativePipelineAction action = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        List<String> restartableStages = PowerMockito.mock(List.class);

        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, this.run);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(action);
        PowerMockito.when(this.run.getParent()).thenReturn(job);
        PowerMockito.when(action.isRestartEnabled()).thenReturn(false);
        PowerMockito.when(restartableStages.contains(underTest.getDisplayName())).thenReturn(true);
        PowerMockito.when(underTest.getStateObj()).thenReturn(BlueRun.BlueRunState.FINISHED);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        assertFalse(underTest.isRestartable());
    }
}
