package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionPoint;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

/**
 *
 * Listener for input step submission event
 *
 * @author Vivek Pandey
 */
public interface PipelineInputStepListener extends ExtensionPoint{
    /**
     * This event is sent when an input step moves from paused to continue state. That is when an input form is submitted.
     *
      * @param inputStep {@link InputStep} which got executed by submitting parameters
     *  @param run {@link WorkflowRun} associated with this input step
     */
    void onStepContinue(InputStep inputStep, WorkflowRun run);
}
