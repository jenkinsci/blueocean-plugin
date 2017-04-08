package io.jenkins.blueocean.service.embedded.rest;

import hudson.ExtensionPoint;
import hudson.tasks.test.TestResult;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import jenkins.model.Jenkins;

public abstract class BlueTestResultFactory implements ExtensionPoint {

    public abstract BlueTestResult getTestResult(TestResult testResult, Reachable parent);

    public static BlueTestResult resolve(TestResult testResult, Reachable parent) {
        for (BlueTestResultFactory factory : Jenkins.getInstance().getExtensionList(BlueTestResultFactory.class)) {
            BlueTestResult result = factory.getTestResult(testResult, parent);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
