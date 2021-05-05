package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.State;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import io.jenkins.blueocean.service.embedded.rest.BlueTestResultContainerImpl;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueTestResultContainerImplTest extends BaseTest {
    static int testsToReturn = 12;

    private BlueTestResultContainerImpl container;

    @Test
    public void testsFilteredByState() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());

        List<BlueTestResult> regressions = Lists.newArrayList(container.getBlueTestResultIterator(allResults, null, State.REGRESSION.name(), null));
        assertEquals(2, regressions.size());
        for (BlueTestResult tr : regressions) {
            assertEquals(State.REGRESSION, tr.getTestState());
        }
    }

    @Test
    public void testsFilteredByStatus() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());
        List<BlueTestResult> allSkipped = Lists.newArrayList(container.getBlueTestResultIterator(allResults, Status.SKIPPED.name(), null, null));
        assertEquals(2, allSkipped.size());
        for (BlueTestResult tr : allSkipped) {
            assertEquals(Status.SKIPPED, tr.getStatus());
        }
    }

    @Test
    public void testsFilteredExistingFailures() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());
        List<BlueTestResult> allNew = Lists.newArrayList(container.getBlueTestResultIterator(allResults, Status.FAILED.name(), null,"2"));
        assertEquals(1, allNew.size());
        for (BlueTestResult tr : allNew) {
            assertEquals(Status.FAILED, tr.getStatus());
            assertThat(tr.getAge(), greaterThan(2));
        }
    }

    @Test
    public void testsNoFiltered() throws Exception {
        List<BlueTestResult> allResults = Lists.newArrayList(container.iterator());
        List<BlueTestResult> all = Lists.newArrayList(container.getBlueTestResultIterator(allResults, null, null, null));
        assertEquals(allResults.size(), all.size());
        assertEquals(allResults, all);
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
                createTestResult(Status.PASSED, State.FIXED, counter, 1),
                createTestResult(Status.PASSED, State.FIXED, counter, 1),
                createTestResult(Status.PASSED, State.UNKNOWN, counter, 1),
                createTestResult(Status.PASSED, State.UNKNOWN, counter, 1),
                createTestResult(Status.PASSED, State.UNKNOWN, counter, 1),
                createTestResult(Status.PASSED, State.UNKNOWN, counter, 1),
                createTestResult(Status.SKIPPED, State.UNKNOWN, counter, 1),
                createTestResult(Status.SKIPPED, State.UNKNOWN, counter, 1),
                createTestResult(Status.FAILED, State.REGRESSION, counter, 1),
                createTestResult(Status.FAILED, State.REGRESSION, counter, 1),
                createTestResult(Status.FAILED, State.UNKNOWN, counter, 1),
                createTestResult(Status.FAILED, State.UNKNOWN, counter, 3)
            );
            return Result.of(Iterables.limit(results, testsToReturn));
        }

        private BlueTestResult createTestResult(Status status, State state, AtomicInteger counter, Integer age) {
            String name = "test_" + counter.incrementAndGet();
            return new BlueTestResultImpl(name, status, state, age);
        }

        class BlueTestResultImpl extends BlueTestResult {

            private String name;
            private Status status;
            private State state;
            private Integer age;

            BlueTestResultImpl(String name, Status status, State state, Integer age) {
                super(null);
                this.name = name;
                this.status = status;
                this.state = state;
                this.age = age;
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
                return age;
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

            @Override
            public boolean hasStdLog()
            {
                return false;
            }

            @Override
            public Link getLink()
            {
                return new Link( getName() );
            }
        }
    }
}
