package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.console.AnnotatedLargeText;
import hudson.model.Action;
import hudson.model.FileParameterValue;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.LogAppender;
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
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
        return ArgumentsAction.getStepArgumentsAsString(node.getNode());
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
    public Object getLog() {
        LogAction logAction = node.getNode().getAction(LogAction.class);
        if(logAction != null){
            final String errorLog = node.blockError();
            if(errorLog != null){
                return new LogResource(logAction.getLogText(), new LogAppender() {
                    @Nonnull
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
        ByteBuffer byteBuffer = new ByteBuffer();
        try {
            byteBuffer.write(msg.getBytes("UTF-8"));
            byteBuffer.close();
            return new LogResource(new AnnotatedLargeText(byteBuffer, Charset.forName("UTF-8"),true, null));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }

    }


    @Override
    public Collection<BlueActionProxy> getActions() {
        // The LogAction is not @ExportedBean but we want to expose its subgraph
        return ActionProxiesImpl.getActionProxies(node.getNode().getActions(), new Predicate<Action>() {
            @Override
            public boolean apply(@Nullable Action input) {
                return input instanceof LogAction;
            }
        }, this);
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
            preSubmissionCheck(execution);

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

    //TODO: InputStepException.preSubmissionCheck() is private, remove it after its made public
    private void preSubmissionCheck(InputStepExecution execution){
        if (execution.isSettled()) {
            throw new ServiceException.BadRequestException("This input has been already given");
        }

        if(!canSubmit(execution.getInput())){
            throw new ServiceException.BadRequestException("You need to be "+ execution.getInput().getSubmitter() +" to submit this");
        }
    }

    private Object parseValue(InputStepExecution execution, JSONArray parameters, StaplerRequest request) throws IOException, InterruptedException {
        Map<String, Object> mapResult = new HashMap<String, Object>();

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
            Authentication a = Jenkins.getAuthentication();
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
    private boolean canSubmit(InputStep inputStep){
        return inputStep.canSubmit();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
