package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
}
