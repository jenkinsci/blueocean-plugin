package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionPoint;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

/**
 *
 * {@link GraphListener} with BlueOcean enhancements
 *
 * @author Vivek Pandey
 */
public interface BlueOceanGraphListener extends ExtensionPoint{
    /**
     * This event is sent when an input step moves from paused to continue state. That is when an input form is submitted.
     *
      * @param inputStep {@link InputStep} which got executed by sumitting parameters
     */
    void onStepContinue(InputStep inputStep, WorkflowRun run);
}
