package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import org.jenkinsci.plugins.workflow.actions.QueueItemAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

        when(nodeBlock.getParents()).thenReturn(Collections.emptyList());
        cause = PipelineNodeUtil.getCauseOfBlockage(stage, null);
        assertNull(cause);

        when(nodeBlock.getParents()).thenReturn(Collections.singletonList(stage));
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
