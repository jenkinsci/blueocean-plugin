package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.Util;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.TestResult;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResultFactory;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class BlueTestResultImpl<T extends TestResult> extends BlueTestResult {

    private final T testResult;
    private final Link parent;

    public BlueTestResultImpl(T testResult, Link parent) {
        this.testResult = testResult;
        this.parent = parent;
    }

    @Override
    public Status getStatus() {
        Status status;
        if (testResult.isPassed()) {
            status = Status.PASSED;
        } else if (testResult.getSkipCount() > 0) {
            status = Status.SKIPPED;
        } else {
            status = Status.FAILED;
        }
        return status;
    }

    @Override
    public State getTestState() {
        State state;
        // TODO: move this to a subclass
        if (testResult instanceof CaseResult) {
            CaseResult caseResult = (CaseResult)testResult;
            switch (caseResult.getStatus()) {
                case REGRESSION:
                    state = State.REGRESSION;
                    break;
                case FIXED:
                    state = State.REGRESSION;
                    break;
                default:
                    state = State.UNKNOWN;
            }
        } else {
            state = State.UNKNOWN;
        }
        return state;
    }

    @Override
    public float getDuration() {
        return testResult.getDuration();
    }

    @Override
    public String getName() {
        return testResult.getFullName();
    }

    @Override
    public String getId() {
        return Util.rawEncode(testResult.getParentAction().getClass().getName()) + ":" + Util.rawEncode(testResult.getId());
    }

    @Override
    public String getStdErr() {
        return serveLog(testResult.getStderr());
    }

    @Override
    public String getStdOut() {
        return serveLog(testResult.getStdout());
    }

    @Override
    public Link getLink() {
        return parent.rel("tests/" + getId());
    }

    private String serveLog(String log) {
        if (isEmpty(log)) {
            throw new NotFoundException("No log");
        }
        return log;
    }

    @Extension(ordinal = -1)
    public static class FactoryImpl extends BlueTestResultFactory {
        @Override
        public BlueTestResult getTestResult(TestResult testResult, Reachable parent) {
            return new BlueTestResultImpl<>(testResult, parent.getLink());
        }
    }
}
