package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.TestResult;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueTestResult;

/**
 * TODO: move to junit plugin
 */
public class BlueJUnitTestResult extends BlueTestResultImpl<CaseResult> {
    public BlueJUnitTestResult(CaseResult testResult, Link parent) {
        super(testResult, parent);
    }

    @Override
    public String getName() {
        return testResult.getPackageName() + " – " + testResult.getName();
    }

    @Override
    public State getTestState() {
        State state;
        switch (testResult.getStatus()) {
            case REGRESSION:
                state = State.REGRESSION;
                break;
            case FIXED:
                state = State.REGRESSION;
                break;
            default:
                state = State.UNKNOWN;
        }
        return state;
    }

    @Extension
    public static class FactoryImpl extends BlueTestResultFactory {
        @Override
        public BlueTestResult getTestResult(TestResult testResult, Reachable parent) {
            if (testResult instanceof CaseResult) {
                return new BlueJUnitTestResult((CaseResult) testResult, parent.getLink());
            }
            return null;
        }
    }
}
