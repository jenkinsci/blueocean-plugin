package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.State;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import io.jenkins.blueocean.service.embedded.rest.BlueTestResultContainerImpl;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueTestResultContainerImplTest extends BaseTest {
    static int testsToReturn = 12;

    private BlueTestResultContainerImpl container;

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
        String id = "io.jenkins.blueocean.service.embedded.BlueTestResultContainerImplTest$FactoryImpl$BlueTestResultImpl:test_1";
        BlueTestResult result = container.get(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    public void testTestsExist() throws Exception {
        List<BlueTestResult> tests = Lists.newArrayList(container.iterator());
        assertEquals(12, tests.size());
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
        container = new BlueTestResultContainerImpl(null, run);
    }

    @TestExtension
    public static class FactoryImpl extends BlueTestResultFactory {
        @Override
        public Result getBlueTestResults(Run<?, ?> run, Reachable parent) {
            AtomicInteger counter = new AtomicInteger(0);
            ImmutableSet<BlueTestResult> results = ImmutableSet.of(
                createTestResult(Status.PASSED, State.FIXED, counter),
                createTestResult(Status.PASSED, State.FIXED, counter),
                createTestResult(Status.PASSED, State.UNKNOWN, counter),
                createTestResult(Status.PASSED, State.UNKNOWN, counter),
                createTestResult(Status.PASSED, State.UNKNOWN, counter),
                createTestResult(Status.PASSED, State.UNKNOWN, counter),
                createTestResult(Status.SKIPPED, State.UNKNOWN, counter),
                createTestResult(Status.SKIPPED, State.UNKNOWN, counter),
                createTestResult(Status.FAILED, State.REGRESSION, counter),
                createTestResult(Status.FAILED, State.REGRESSION, counter),
                createTestResult(Status.FAILED, State.UNKNOWN, counter),
                createTestResult(Status.FAILED, State.UNKNOWN, counter)
            );
            return Result.of(Iterables.limit(results, testsToReturn));
        }

        private BlueTestResult createTestResult(Status status, State state, AtomicInteger counter) {
            String name = "test_" + counter.incrementAndGet();
            return new BlueTestResultImpl(name, status, state);
        }

        class BlueTestResultImpl extends BlueTestResult {

            private String name;
            private Status status;
            private State state;

            BlueTestResultImpl(String name, Status status, State state) {
                super(null);
                this.name = name;
                this.status = status;
                this.state = state;
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
                return name;
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
                return name;
            }
        }
    }
}
