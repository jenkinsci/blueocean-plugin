package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WorkflowRun.class, WorkflowJob.class, QueueUtil.class })
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*"})
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
}