package io.jenkins.blueocean.service.embedded;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory.Result;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.State;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueTestResultFactoryTest extends BaseTest {

    static int testsToReturn = 12;

    @Test
    public void testFactory() {
        Run<?, ?> run = mock(Run.class);
        Result resolved = BlueTestResultFactory.resolve(run, null);
        assertNotNull(resolved.summary);
        assertEquals(12, resolved.summary.getTotal());
        assertEquals(6, resolved.summary.getPassedTotal());
        assertEquals(2, resolved.summary.getSkippedTotal());
        assertEquals(4, resolved.summary.getFailedTotal());
        assertEquals(2, resolved.summary.getRegressionsTotal());
        assertEquals(2, resolved.summary.getFixedTotal());
        assertEquals(2, resolved.summary.getExistingFailedTotal());
    }

    @Test
    public void testFactoryReturnsZeroTests() {
        testsToReturn = 0;
        Run<?, ?> run = mock(Run.class);
        Result resolved = BlueTestResultFactory.resolve(run, null);
        assertNull(resolved.summary);
        assertNull(resolved.results);
    }

    @Test
    public void testFactoryReturnsZeroTestsForNode() {
        testsToReturn = 0;
        Run<?, ?> run = mock(Run.class);
        BluePipelineNode node = mock(BluePipelineNode.class);
        Result resolved = BlueTestResultFactory.resolve(run, node);
        assertNull(resolved.summary);
        assertNull(resolved.results);
    }

    @TestExtension
    public static class FactoryImpl extends BlueTestResultFactory {
        @Override
        public Result getBlueTestResults(Run<?, ?> run, Reachable parent) {
            Set<BlueTestResult> results = new HashSet<>(Arrays.asList(
                createTestResult(Status.PASSED, State.FIXED),
                createTestResult(Status.PASSED, State.FIXED),
                createTestResult(Status.PASSED, State.UNKNOWN),
                createTestResult(Status.PASSED, State.UNKNOWN),
                createTestResult(Status.PASSED, State.UNKNOWN),
                createTestResult(Status.PASSED, State.UNKNOWN),
                createTestResult(Status.SKIPPED, State.UNKNOWN),
                createTestResult(Status.SKIPPED, State.UNKNOWN),
                createTestResult(Status.FAILED, State.REGRESSION),
                createTestResult(Status.FAILED, State.REGRESSION),
                createTestResult(Status.FAILED, State.UNKNOWN),
                createTestResult(Status.FAILED, State.UNKNOWN)
            ));
            return Result.of(results.stream().limit(testsToReturn).collect(Collectors.toList()));
        }

        private BlueTestResult createTestResult(Status status, State state) {
            BlueTestResult result = mock(BlueTestResult.class);
            when(result.getStatus()).thenReturn(status);
            when(result.getTestState()).thenReturn(state);
            return result;
        }
    }
}
