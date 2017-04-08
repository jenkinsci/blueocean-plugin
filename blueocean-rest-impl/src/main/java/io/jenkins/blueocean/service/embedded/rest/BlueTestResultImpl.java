package io.jenkins.blueocean.service.embedded.rest;

import hudson.Util;
import hudson.tasks.test.TestResult;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueTestResult;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class BlueTestResultImpl<T extends TestResult> extends BlueTestResult {

    protected final T testResult;
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
        return State.UNKNOWN;
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
    public String getErrorStackTrace() {
        return testResult.getErrorStackTrace();
    }

    @Override
    public String getErrorDetails() {
        return testResult.getErrorDetails();
    }

    @Override
    public String getId() {
        return Util.rawEncode(testResult.getParentAction().getClass().getName()) + ":" + Util.rawEncode(testResult.getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAge() {
        if(testResult.isPassed())
            return 0;
        else if (testResult.getRun() != null) {
            return testResult.getRun().getNumber()-testResult.getFailedSince()+1;
        } else {
            return 0;
        }
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
}
