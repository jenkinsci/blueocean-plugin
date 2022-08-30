package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.ExtensionList;
import hudson.FilePath;
import hudson.console.AnnotatedLargeText;
import hudson.model.Failure;
import hudson.model.FileParameterValue;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import io.jenkins.blueocean.commons.JSON;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.LogAppender;
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;
import org.springframework.security.core.Authentication;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author Vivek Pandey
 */
public class PipelineStepImpl extends BluePipelineStep {
    private final FlowNodeWrapper node;
    private final Link self;

    public static final String PARAMETERS_ELEMENT="parameters";
    public static final String ID_ELEMENT="id";
    public static final String ABORT_ELEMENT="abort";
    public static final String NAME_ELEMENT="name";

    public PipelineStepImpl(FlowNodeWrapper node, Link parent) {
        assert node != null;
        this.self = parent.rel(node.getId());
        this.node = node;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return node.getNode().getDisplayName();
    }

    @Override
    public String getDisplayDescription() {
        String displayDescription = ArgumentsAction.getStepArgumentsAsString(node.getNode());
        if (displayDescription != null) {
            // JENKINS-45099 Remove any control characters that may have found their way out of a script
            displayDescription = JSON.sanitizeString(displayDescription);
        }
        return displayDescription;
    }

    @Override
    public String getType() {
        return node.getType().name();
    }

    @Override
    public String getStepType() {
        FlowNode flowNode = this.node.getNode();
        if (flowNode instanceof StepNode && !(flowNode instanceof StepEndNode)) {
            StepNode stepNode = (StepNode) flowNode;
            StepDescriptor descriptor = stepNode.getDescriptor();
            if (descriptor != null) return descriptor.getId();
        }
        return "unknown";
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return node.getStatus().getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        return node.getStatus().getState();
    }

    @Override
    public Date getStartTime() {
        return new Date(node.getTiming().getStartTimeMillis());
    }

    @Override
    public Long getDurationInMillis() {
        return node.getTiming().getTotalDurationMillis();
    }

    @Override
    public String getStartTimeString(){
        return AbstractRunImpl.DATE_FORMAT.format(getStartTime().toInstant());
    }

    @Override
    public Object getLog() {
        LogAction logAction = node.getNode().getAction(LogAction.class);
        if(logAction != null){
            final String errorLog = node.blockError();
            if(errorLog != null){
                return new LogResource(logAction.getLogText(), new LogAppender() {
                    @NonNull
                    @Override
                    public Reader getLog() {
                        return new StringReader(errorLog+"\n");
                    }
                });
            }
            return new LogResource(logAction.getLogText());
        }else{
            return getLogResource(node);
        }
    }

    FlowNodeWrapper getFlowNodeWrapper(){
        return node;
    }

    @SuppressWarnings("unchecked")
    private static LogResource getLogResource(FlowNodeWrapper  node){
        String msg=node.nodeError();
        if(msg == null){
            msg = node.blockError();
        }
        if(msg == null){
            return null;
        }
        msg = msg + "\n";

        try (ByteBuffer byteBuffer = new ByteBuffer()) {
            byteBuffer.write(msg.getBytes(StandardCharsets.UTF_8));
            return new LogResource(new AnnotatedLargeText( byteBuffer, StandardCharsets.UTF_8, true, null));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }

    }


    @Override
    public Collection<BlueActionProxy> getActions() {
        // The LogAction is not @ExportedBean but we want to expose its subgraph
        return ActionProxiesImpl.getActionProxies(node.getNode().getActions(), input ->  input instanceof LogAction, this);
    }

    @Override
    public BlueInputStep getInputStep() {
        InputStep inputStep = node.getInputStep();
        if(inputStep != null){
            return new InputStepImpl(inputStep, this);
        }
        return null;
    }

    @Override
    public HttpResponse submitInputStep(StaplerRequest request) {
        JSONObject body;
        try {
            body = JSONObject.fromObject(IOUtils.toString(request.getReader()));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
        String id = body.getString(ID_ELEMENT);
        if(id == null){
            throw new ServiceException.BadRequestException("id is required");
        }

        if(body.get(PARAMETERS_ELEMENT) == null && body.get(ABORT_ELEMENT) == null){
            throw new ServiceException.BadRequestException("parameters is required");
        }

        WorkflowRun run = node.getRun();
        InputAction inputAction = run.getAction(InputAction.class);
        if (inputAction == null) {
            throw new ServiceException.BadRequestException("Error processing Input Submit request. This Run instance does not" +
                    " have an InputAction.");
        }

        try {
            InputStepExecution execution = inputAction.getExecution(id);
            if (execution == null) {
                throw new ServiceException.BadRequestException(
                        String.format("Error processing Input Submit request. This Run instance does not" +
                        " have an Input with an id of '%s'.", id));
            }
            //if abort, abort and return
            if(body.get(ABORT_ELEMENT) != null && body.getBoolean(ABORT_ELEMENT)){
                return execution.doAbort();
            }

            //XXX: execution.doProceed(request) expects submitted form, otherwise we could have simply used it

            try {
                execution.preSubmissionCheck();
            } catch (Failure f) {
                throw new ServiceException.BadRequestException(f.getMessage());
            }

            Object o = parseValue(execution, JSONArray.fromObject(body.get(PARAMETERS_ELEMENT)), request);

            HttpResponse response =  execution.proceed(o);
            for(PipelineInputStepListener listener: ExtensionList.lookup(PipelineInputStepListener.class)){
                listener.onStepContinue(execution.getInput(), run);
            }
            return response;
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new ServiceException.UnexpectedErrorException("Error processing Input Submit request."+e.getMessage());
        }
    }

    private Object parseValue(InputStepExecution execution, JSONArray parameters, StaplerRequest request) throws IOException, InterruptedException {
        Map<String, Object> mapResult = new HashMap<>();

        InputStep input = execution.getInput();
        for(Object o: parameters){
            JSONObject p = (JSONObject) o;
            String name = (String) p.get(NAME_ELEMENT);

            if(name == null){
                throw new ServiceException.BadRequestException("name is required parameter element");
            }

            ParameterDefinition d=null;
            for (ParameterDefinition def : input.getParameters()) {
                if (def.getName().equals(name))
                    d = def;
            }
            if (d == null)
                throw new ServiceException.BadRequestException("No such parameter definition: " + name);

            ParameterValue v = d.createValue(request, p);
            if (v == null) {
                continue;
            }
            mapResult.put(name, convert(name, v));
        }
        // If a destination value is specified, push the submitter to it.
        String valueName = input.getSubmitterParameter();
        if (valueName != null && !valueName.isEmpty()) {
            Authentication a = Jenkins.getAuthentication2();
            mapResult.put(valueName, a.getName());
        }
        switch (mapResult.size()) {
            case 0:
                return null;    // no value if there's no parameter
            case 1:
                return mapResult.values().iterator().next();
            default:
                return mapResult;
        }
    }


    private Object convert(String name, ParameterValue v) throws IOException, InterruptedException {
        if (v instanceof FileParameterValue) {
            FileParameterValue fv = (FileParameterValue) v;
            FilePath fp = new FilePath(node.getRun().getRootDir()).child(name);
            fp.copyFrom(fv.getFile());
            return fp;
        } else {
            return v.getValue();
        }
    }

    @Override
    public Link getLink() {
        return self;
    }
}
