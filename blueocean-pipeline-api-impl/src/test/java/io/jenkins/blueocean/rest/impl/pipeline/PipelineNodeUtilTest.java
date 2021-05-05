package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.ImmutableList;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import org.jenkinsci.plugins.workflow.actions.QueueItemAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(QueueItemAction.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class PipelineNodeUtilTest {
    /**
     * This test tests all code paths of getCauseOfBlockage.
     * @throws Exception
     */
    @Test
    public void getCauseOfBlockage() throws Exception {
        CauseOfBlockage blockage = mock(CauseOfBlockage.class);
        CauseOfBlockage taskBlockage = mock(CauseOfBlockage.class);

        FlowNode stage = mock(FlowNode.class);
        FlowNode nodeBlock = mock(FlowNode.class);
        Queue.Item item = mock(Queue.Item.class);
        mockStatic(QueueItemAction.class);
        String cause = null;

        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(nodeBlock.getParents()).thenReturn(ImmutableList.of());
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(nodeBlock.getParents()).thenReturn(ImmutableList.of(stage));
        when(QueueItemAction.getQueueItem(nodeBlock)).thenReturn(null);
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(QueueItemAction.getQueueItem(nodeBlock)).thenReturn(item);
        when(item.getCauseOfBlockage()).thenReturn(null);
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(blockage.getShortDescription()).thenReturn("test");
        when(item.getCauseOfBlockage()).thenReturn(blockage);
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, nodeBlock);
        assertEquals("test", cause);

        when(blockage.getShortDescription()).thenReturn(null);
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(taskBlockage.getShortDescription()).thenReturn("test1");
        Whitebox.setInternalState(item,"task", mock(Queue.Task.class));
        when(item.task.getCauseOfBlockage()).thenReturn(taskBlockage);
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, nodeBlock);
        assertEquals("test1", cause);
    }
}
