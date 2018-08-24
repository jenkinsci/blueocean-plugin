package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.Run;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BlueRunContainer {

    private static Logger LOGGER = LoggerFactory.getLogger( RunContainerImpl.class );

    private final Job job;
    private final BluePipeline pipeline;

    public RunContainerImpl(@Nonnull BluePipeline pipeline, @Nonnull Job job) {
        this.job = job;
        this.pipeline = pipeline;
    }

    @Override
    public Link getLink() {
        return pipeline.getLink().rel("runs");
    }

    @Override
    public BlueRun get(String runId) {
        RunList<? extends hudson.model.Run> runList = job.getBuilds();
        Run run;
        if (runId != null) {
            int number;
            try
            {
                number = Integer.parseInt( runId );
            }
            catch ( NumberFormatException e )
            {
                throw new NotFoundException(
                    String.format( "Run %s not found in organization %s and pipeline %s", runId, pipeline.getOrganizationName(), job.getName() ) );
            }
            run = findRun( runList, number );
            if( run == null)
            {
                BlueRun blueRun = findBlueQueueItem( QueueUtil.getQueuedItems( pipeline.getOrganization(), job ), number);
                if(blueRun != null) return blueRun;

                // JENKINS-53175 so we try again as the build has maybe from out of the queue and running now or has been running
                runList = job.getBuilds();
                run = findRun( runList, number );
                if ( run == null )
                {
                    run = job.getBuildByNumber( number );
                    if ( run == null )
                    {
                        // still so try again the queue...
                        List<BlueQueueItem> blueQueueItems = QueueUtil.getQueuedItems( pipeline.getOrganization(), job );
                        blueRun = findBlueQueueItem( blueQueueItems, number );
                        if(blueRun != null) return blueRun;
                        if(!job.isBuilding() && !job.isInQueue())
                        {
//                            // let's try a last time with sleep (olamy: dohhhh seriously)
//                            try
//                            {
//                                Thread.sleep( 100 );
//                            }
//                            catch ( InterruptedException e )
//                            {
//                                //no op
//                            }
                            runList = job.getBuilds();
                            run = findRun( runList, number );
                            if (run == null)
                            {
                                generateThreadDump();
                                // so definitely no luck.... so log that and return null
                                LOGGER.warn(
                                    "Cannot find run with number: {}, runId: {}, job.name: {} in runList: {}, queueList: {}, jenkinsQueue: {}, job.isBuilding {}, job.isInQueue {}",
                                    //
                                    number, runId, job.getName(), runList, blueQueueItems, ( job instanceof BuildableItem ) ? Jenkins.get().getQueue().getItems(
                                        ( (BuildableItem) job ) ) : null, job.isBuilding(), job.isInQueue() );
                                throw new NotFoundException(
                                    String.format( "Run %s not found in organization %s and pipeline %s", runId,
                                                   pipeline.getOrganizationName(), job.getName() ) );
                            }
                        }
                    }
                }
            }
        } else {
            run = runList.getLastBuild();
        }
        return BlueRunFactory.getRun(run, pipeline);
    }

    private void generateThreadDump() {
//        StringBuilder dump = new StringBuilder( );
//        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
//        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo( threadMXBean.getAllThreadIds(), 100 );
//        for(ThreadInfo threadInfo : threadInfos)
//        {
//            dump.append('"');
//            dump.append(threadInfo.getThreadName());
//            dump.append("\" ");
//            final Thread.State state = threadInfo.getThreadState();
//            dump.append("\n   java.lang.Thread.State: ");
//            dump.append(state);
//            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
//            for (final StackTraceElement stackTraceElement : stackTraceElements) {
//                dump.append("\n        at ");
//                dump.append(stackTraceElement);
//            }
//            dump.append("\n\n");
//        }

        ThreadInfo[]  threadInfos = ManagementFactory.getThreadMXBean()
            .dumpAllThreads(true,
                            true);
        StringBuilder dump        = new StringBuilder();
        dump.append(String.format("%n"));
        for (ThreadInfo threadInfo : threadInfos) {
            dump.append(threadInfo);
        }

        LOGGER.info( dump.toString() );
    }

    private Run findRun(RunList<? extends hudson.model.Run> runList, int runId){
        for(Run run : runList)
        {
            if(run.getNumber() == runId)
            {
                return run;
            }
        }
        return null;
    }

    private BlueRun findBlueQueueItem(List<BlueQueueItem> queueItems, int number)
    {
        for ( BlueQueueItem item : QueueUtil.getQueuedItems( pipeline.getOrganization(), job ) )
        {
            if ( item.getExpectedBuildNumber() == number )
            {
                return item.toRun();
            }
        }
        return null;
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

    private List<ParameterValue> getParameterValue(@Nonnull StaplerRequest request) {
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
