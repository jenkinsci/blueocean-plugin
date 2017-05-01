package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import hudson.ExtensionList;
import hudson.console.AnnotatedLargeText;
import hudson.model.Action;
import hudson.model.Failure;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.LogAppender;
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
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
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
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
            throw new ServiceException.BadRequestExpception("id is required");
        }

        if(body.get(PARAMETERS_ELEMENT) == null && body.get(ABORT_ELEMENT) == null){
            throw new ServiceException.BadRequestExpception("parameters is required");
        }

        WorkflowRun run = node.getRun();
        InputAction inputAction = run.getAction(InputAction.class);
        if (inputAction == null) {
            throw new ServiceException.BadRequestExpception("Error processing Input Submit request. This Run instance does not" +
                    " have an InputAction.");
        }

        try {
            InputStepExecution execution = inputAction.getExecution(id);
            if (execution == null) {
                throw new ServiceException.BadRequestExpception(
                        String.format("Error processing Input Submit request. This Run instance does not" +
                        " have an Input with an id of '%s'.", id));
            }
            //if abort, abort and return
            if(body.get(ABORT_ELEMENT) != null && body.getBoolean(ABORT_ELEMENT)){
                return execution.doAbort();
            }
            
            preSubmissionCheck(execution);
            Object v = body.get(PARAMETERS_ELEMENT);
            JSONArray params =  v != null ? JSONArray.fromObject(body.get(PARAMETERS_ELEMENT)) : null;
            HttpResponse response = execution.proceed(params, request);
            for(PipelineInputStepListener listener: ExtensionList.lookup(PipelineInputStepListener.class)){
                listener.onStepContinue(execution.getInput(), run);
            }
            return response;
        } catch (IOException | InterruptedException | TimeoutException | ServletException e) {
            throw new ServiceException.UnexpectedErrorException("Error processing Input Submit request."+e.getMessage());
        }
    }

    private void preSubmissionCheck(InputStepExecution execution){
        try{
            execution.preSubmissionCheck();
        }catch (Failure e){
            throw new ServiceException.BadRequestExpception(e.getMessage(), e);
        }
    }

    @Override
    public Link getLink() {
        return self;
    }
}
