package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.FilePath;
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
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        if(PipelineNodeUtil.isLoggable.apply(node.getNode())){
            return new LogResource(node.getNode().getAction(LogAction.class).getLogText());
        }
        return null;
    }


    @Override
    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(node.getNode().getActions(), this);
    }

    @Override
    public BlueInputStep getInputStep() {
        if(node.getInputStep() != null){
            return new InputStepImpl(node.getInputStep(), this);
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

        InputStepExecution execution = inputAction.getExecution(id);
        if (execution == null) {
            throw new ServiceException.BadRequestExpception(
                    String.format("Error processing Input Submit request. This Run instance does not" +
                    " have an Input with an id of '%s'.", id));
        }
        try {
            //if abort, abort and return
            if(body.get(ABORT_ELEMENT) != null && body.getBoolean(ABORT_ELEMENT)){
                return execution.doAbort();
            }

            //XXX: execution.doProceed(request) expects submitted form, otherwise we could have simply used it
            preSubmissionCheck(execution);

            Object o = parseValue(execution, JSONArray.fromObject(body.get(PARAMETERS_ELEMENT)), request);

            return execution.proceed(o);
        } catch (IOException | InterruptedException | ServletException e) {
            throw new ServiceException.UnexpectedErrorException("Error processing Input Submit request."+e.getMessage());
        }
    }

    //TODO: InputStepException.preSubmissionCheck() is private, remove it after its made public
    private void preSubmissionCheck(InputStepExecution execution){
        if (execution.isSettled()) {
            throw new ServiceException.BadRequestExpception("This input has been already given");
        }

        if(!canSubmit(execution.getInput())){
            throw new ServiceException.BadRequestExpception("You need to be "+ execution.getInput().getSubmitter() +" to submit this");
        }
    }

    private Object parseValue(InputStepExecution execution, JSONArray parameters, StaplerRequest request) throws IOException, InterruptedException {
        Map<String, Object> mapResult = new HashMap<String, Object>();

        for(Object o: parameters){
            JSONObject p = (JSONObject) o;
            String name = (String) p.get(NAME_ELEMENT);

            if(name == null){
                throw new ServiceException.BadRequestExpception("name is required parameter element");
            }

            ParameterDefinition d=null;
            for (ParameterDefinition def : execution.getInput().getParameters()) {
                if (def.getName().equals(name))
                    d = def;
            }
            if (d == null)
                throw new ServiceException.BadRequestExpception("No such parameter definition: " + name);

            ParameterValue v = d.createValue(request, p);
            if (v == null) {
                continue;
            }
            mapResult.put(name, convert(name, v));
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

        Authentication a = Jenkins.getAuthentication();
        String submitter = inputStep.getSubmitter();
        if (submitter==null || a.getName().equals(submitter)) {
            return true;
        }
        for (GrantedAuthority ga : a.getAuthorities()) {
            if (ga.getAuthority().equals(submitter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Link getLink() {
        return self;
    }
}
