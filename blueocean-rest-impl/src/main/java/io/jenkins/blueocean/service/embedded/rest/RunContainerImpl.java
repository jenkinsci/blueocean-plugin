package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.queue.ScheduleResult;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BlueRunContainer {

    private final Job job;
    private final BluePipeline pipeline;

    public RunContainerImpl(@NonNull BluePipeline pipeline, @NonNull Job job) {
        this.job = job;
        this.pipeline = pipeline;
    }

    @Override
    public Link getLink() {
        return pipeline.getLink().rel("runs");
    }

    @Override
    public BlueRun get(String name) {
        RunList<? extends hudson.model.Run> runList = job.getBuilds();

        if (name == null) {
            return BlueRunFactory.getRun(runList.getLastBuild(), pipeline);
        }

        for (hudson.model.Run r : runList) {
            if (r.getId().equals(name)) {
                return BlueRunFactory.getRun(r, pipeline);
            }
        }

        int number;
        try {
            number = Integer.parseInt(name);
        } catch (NumberFormatException e) {
            throw new NotFoundException(String.format("Run %s not found in organization %s and pipeline %s",
                    name, pipeline.getOrganizationName(), job.getName()));
        }
        for (BlueQueueItem item : QueueUtil.getQueuedItems(pipeline.getOrganization(), job)) {
            if (item.getExpectedBuildNumber() == number) {
                return item.toRun();
            }
        }
        throw new NotFoundException(
            String.format("Run %s not found in organization %s and pipeline %s",
                name, pipeline.getOrganizationName(), job.getName()));
    }

    @Override
    public Iterator<BlueRun> iterator() {
        return getRuns(RunSearch.findRuns(job, pipeline.getLink()));
    }

    @Override
    public Iterator<BlueRun> iterator(int start, int limit) {
        return getRuns(RunSearch.findRuns(job, pipeline.getLink(), start, limit));
    }

    private Iterator<BlueRun> getRuns(Iterable<BlueRun> runs) {
        return Iterables.concat(Iterables.transform(QueueUtil.getQueuedItems(pipeline.getOrganization(), job), new Function<BlueQueueItem, BlueRun>() {
            @Override
            public BlueRun apply(BlueQueueItem input) {
                return input.toRun();
            }
        }), runs).iterator();
    }

    /**
     * Schedules a build. If build already exists in the queue and the pipeline does not
     * support running multiple builds at the same time, return a reference to the existing
     * build.
     *
     * @return Queue item.
     */
    @Override
    public BlueRun create(StaplerRequest request) {
        job.checkPermission(Item.BUILD);
        if (job instanceof Queue.Task) {
            ScheduleResult scheduleResult;

            List<ParameterValue> parameterValues = getParameterValue(request);
            int expectedBuildNumber = job.getNextBuildNumber();
            if(parameterValues.size() > 0) {
                scheduleResult = Jenkins.getInstance()
                        .getQueue()
                        .schedule2((Queue.Task) job, 0, new ParametersAction(parameterValues),
                                new CauseAction(new Cause.UserIdCause()));
            }else {
                scheduleResult = Jenkins.getInstance()
                        .getQueue()
                        .schedule2((Queue.Task) job, 0, new CauseAction(new Cause.UserIdCause()));
            }
            // Keep FB happy.
            // scheduleResult.getItem() will always return non-null if scheduleResult.isAccepted() is true
            final Queue.Item item = scheduleResult.getItem();
            if(scheduleResult.isAccepted() && item != null) {
                return new QueueItemImpl(
                    pipeline.getOrganization(),
                    item,
                    pipeline,
                    expectedBuildNumber, pipeline.getLink().rel("queue").rel(Long.toString(item.getId())),
                    pipeline.getLink()
                ).toRun();
            } else {
                throw new ServiceException.UnexpectedErrorException("Queue item request was not accepted");
            }
        } else {
            throw new ServiceException.NotImplementedException("This pipeline type does not support being queued.");
        }
    }

    private List<ParameterValue> getParameterValue(@NonNull StaplerRequest request) {
        List<ParameterValue> values = new ArrayList<>();
        List<ParameterDefinition> pdsInRequest = new ArrayList<>();
        ParametersDefinitionProperty pp = (ParametersDefinitionProperty) job.getProperty(ParametersDefinitionProperty.class);
        if (pp == null) {
            return values;
        }
        try {
            JSONObject body = JSONObject.fromObject(IOUtils.toString(request.getReader()));
            if (body.get("parameters") == null && pp.getParameterDefinitions().size() > 0) {
                throw new ServiceException.BadRequestException("This is parameterized job, requires parameters");
            }
            if (body.get("parameters") != null) {
                JSONArray pds = JSONArray.fromObject(body.get("parameters"));
                for (Object o : pds) {
                    JSONObject p = (JSONObject) o;
                    String name = (String) p.get("name");
                    if (name == null) {
                        throw new ServiceException.BadRequestException("parameters.name is required element");
                    }
                    ParameterDefinition pd = pp.getParameterDefinition(name);
                    if (pd == null) {
                        throw new ServiceException.BadRequestException("No such parameter definition: " + name);
                    }
                    ParameterValue parameterValue = pd.createValue(request, p);
                    if (parameterValue != null) {
                        values.add(parameterValue);
                        pdsInRequest.add(pd);
                    } else {
                        throw new ServiceException.BadRequestException("Invalid value. Cannot retrieve the parameter value: " + name);
                    }
                }

                //now check for missing parameters without default values
                if(pdsInRequest.size() != pp.getParameterDefinitions().size()){
                    for(ParameterDefinition pd:pp.getParameterDefinitions()){
                        if(!pdsInRequest.contains(pd)){
                            ParameterValue v = pd.getDefaultParameterValue();
                            if(v == null || v.getValue() == null){
                                throw new ServiceException.BadRequestException("Missing parameter: "+pd.getName());
                            }
                            values.add(v);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
        return values;
    }


}
