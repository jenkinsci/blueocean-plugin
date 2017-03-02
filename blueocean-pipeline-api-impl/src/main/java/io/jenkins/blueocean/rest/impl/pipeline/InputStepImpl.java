package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Vivek Pandey
 */
public class InputStepImpl extends BlueInputStep {

    private InputStep inputStep;
    private Link self;

    public InputStepImpl(InputStep inputStep, Reachable parent) {
        this.inputStep = inputStep;
        if (inputStep.getId() == null) {
            // Make sure the input step has an ID.
            this.inputStep.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        this.self = parent.getLink().rel("input");
    }

    @Override
    public String getId() {
        return inputStep.getId();
    }

    @Override
    public String getMessage() {
        return inputStep.getMessage();
    }

    @Override
    public String getSubmitter() {
        return inputStep.getSubmitter();
    }

    @Override
    public String getOk() {
        return inputStep.getOk();
    }

    @Override
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        params.addAll(inputStep.getParameters());
        return params;
    }

    @Override
    public Link getLink() {
        return self;
    }
}
