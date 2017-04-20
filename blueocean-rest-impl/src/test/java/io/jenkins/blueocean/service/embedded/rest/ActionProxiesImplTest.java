package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hudson.model.Action;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import org.junit.Test;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActionProxiesImplTest {

    @Test
    public void testGetActionProxies() throws Exception {
        List<? extends Action> actions = ImmutableList.of(new TestAction(), new Test2Action(), new Test3Action(), new Test4Action());
        assertTrue(ActionProxiesImpl.getActionProxies(ImmutableList.<String>of(), actions, null).isEmpty());

        List<BlueActionProxy> actionProxies = Lists.newArrayList(ActionProxiesImpl.getActionProxies(ImmutableList.of(TestAction.class.getName()), actions, null));
        assertEquals(1, actionProxies.size());
        assertEquals(TestAction.class.getName(), actionProxies.get(0).get_Class());

        actionProxies = Lists.newArrayList(ActionProxiesImpl.getActionProxies(
            ImmutableList.of(
                TestAction.class.getName(),
                Test2Action.class.getName(),
                Test3Action.class.getName(), // Not exportable
                Test4Action.class.getName()
            ),
            actions,
            null));

        assertEquals(3, actionProxies.size());
    }

    @ExportedBean
    private class TestAction extends AbstractTestAction {}
    @ExportedBean
    private class Test2Action extends AbstractTestAction {}
    // Not exported
    private class Test3Action extends AbstractTestAction {}
    @ExportedBean
    private class Test4Action extends AbstractTestAction {}

    class AbstractTestAction implements Action {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return null;
        }
    }
}
