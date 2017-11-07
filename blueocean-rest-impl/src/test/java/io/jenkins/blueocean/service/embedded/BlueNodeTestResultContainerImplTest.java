package io.jenkins.blueocean.service.embedded;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.State;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import io.jenkins.blueocean.service.embedded.rest.BlueNodeTestResultContainerImpl;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueNodeTestResultContainerImplTest extends BaseTest {
    static int testsToReturn = 12;

    private BlueNodeTestResultContainerImpl container;

    @Test
    public void testsFilteredByState() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());
        List<BlueTestResult> regressions = Lists.newArrayList(BlueNodeTestResultContainerImpl.filterByState(allResults, State.REGRESSION.name()));
        assertEquals(1, regressions.size());
        for (BlueTestResult tr : regressions) {
            assertEquals(State.REGRESSION, tr.getTestState());
        }
    }

    @Test
    public void testsFilteredByStatus() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());
        List<BlueTestResult> allSkipped = Lists.newArrayList(BlueNodeTestResultContainerImpl.filterByStatus(allResults, Status.SKIPPED.name()));
        assertEquals(1, allSkipped.size());
        for (BlueTestResult tr : allSkipped) {
            assertEquals(Status.SKIPPED, tr.getStatus());
        }
    }

    @Test
    public void testGetTestExists() throws Exception {
        boolean caught = false;
        try {
            container.get("does not exist");
        } catch (NotFoundException e) {
            caught = true;
        }
        assertTrue("should not exist", caught);
    }

    @Test
    public void testGetTestNotFound() throws Exception {
        String id = "io.jenkins.blueocean.service.embedded.BlueNodeTestResultContainerImplTest$FactoryImpl$BlueTestResultImpl:1__test_1";
        BlueTestResult result = container.get(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    public void testTestsExist() throws Exception {
        List<BlueTestResult> tests = Lists.newArrayList(container.iterator());
        assertEquals(6, tests.size());
    }

    @Test
    public void testTestsNotFound() throws Exception {
        testsToReturn = 0;
        boolean caught = false;
        try {
            Lists.newArrayList(container.iterator());
        } catch (NotFoundException e) {
            caught = true;
        }
        assertTrue("should not exist", caught);
    }

    public void setup() {
        testsToReturn = 12;
        Run<?, ?> run = mock(Run.class);
        when(run.getAction(TestResultAction.class)).thenReturn(null);
        BluePipelineNode node = mock(BluePipelineNode.class);
        when(node.getId()).thenReturn("1");
        container = new BlueNodeTestResultContainerImpl(null, run, node);
    }

    @TestExtension
    public static class FactoryImpl extends BlueTestResultFactory {
        @Override
        public Result getBlueTestResults(Run<?, ?> run, Reachable parent) {
            AtomicInteger counter = new AtomicInteger(0);
            ImmutableSet<BlueTestResult> results = ImmutableSet.of(
                createTestResult(Status.PASSED, State.FIXED, "1", counter),
                createTestResult(Status.PASSED, State.FIXED, "2", counter),
                createTestResult(Status.PASSED, State.UNKNOWN, "1", counter),
                createTestResult(Status.PASSED, State.UNKNOWN, "2", counter),
                createTestResult(Status.PASSED, State.UNKNOWN, "1", counter),
                createTestResult(Status.PASSED, State.UNKNOWN, "2", counter),
                createTestResult(Status.SKIPPED, State.UNKNOWN, "1", counter),
                createTestResult(Status.SKIPPED, State.UNKNOWN, "2", counter),
                createTestResult(Status.FAILED, State.REGRESSION, "1", counter),
                createTestResult(Status.FAILED, State.REGRESSION, "2", counter),
                createTestResult(Status.FAILED, State.UNKNOWN, "1", counter),
                createTestResult(Status.FAILED, State.UNKNOWN, "2", counter)
            );
            return Result.of(Iterables.limit(results, testsToReturn));
        }

        @Override
        public Result getBlueTestResults(Run<?, ?> run, BluePipelineNode node, Reachable parent) {
            return Result.of(Iterables.limit(Iterables.filter(getBlueTestResults(run, parent).results, new Predicate<BlueTestResult>() {
                @Override
                public boolean apply(@Nullable BlueTestResult input) {
                    return input instanceof BlueTestResultImpl && ((BlueTestResultImpl) input).getNodeName().equals(node.getId());
                }
            }), testsToReturn));
        }

        private BlueTestResult createTestResult(Status status, State state, String nodeName, AtomicInteger counter) {
            String name = "test_" + counter.incrementAndGet();
            return new BlueTestResultImpl(name, status, state, nodeName);
        }

        class BlueTestResultImpl extends BlueTestResult {

            private String name;
            private Status status;
            private State state;
            private String nodeName;

            BlueTestResultImpl(String name, Status status, State state, String nodeName) {
                super(null);
                this.name = name;
                this.status = status;
                this.state = state;
                this.nodeName = nodeName;
            }

            @Override
            public Status getStatus() {
                return status;
            }

            @Override
            public State getTestState() {
                return state;
            }

            @Override
            public float getDuration() {
                return 0;
            }

            @Override
            public int getAge() {
                return 0;
            }

            @Override
            public String getName() {
                if (nodeName == null) {
                    return name;
                } else {
                    return nodeName + " / " + name;
                }
            }

            @Override
            public String getErrorStackTrace() {
                return null;
            }

            @Override
            public String getErrorDetails() {
                return null;
            }

            @Override
            public String getStdErr() {
                return null;
            }

            @Override
            public String getStdOut() {
                return null;
            }

            @Override
            protected String getUniqueId() {
                if (nodeName == null) {
                    return name;
                } else {
                    return nodeName + "__" + name;
                }
            }

            public String getNodeName() {
                return nodeName;
            }
        }
    }
}
