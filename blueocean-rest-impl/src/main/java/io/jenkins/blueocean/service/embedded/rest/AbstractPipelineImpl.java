package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Pipeline abstraction implementation. Use it to extend other kind of jenkins jobs
 *
 * @author Vivek Pandey
 */
@Capability("hudson.model.Job")
public class AbstractPipelineImpl extends BluePipeline {
    private final Job job;

    protected AbstractPipelineImpl(Job job) {
        this.job = job;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getName() {
        return job.getName();
    }

    @Override
    public String getDisplayName() {
        return job.getDisplayName();
    }

    @Override
    public Integer getWeatherScore() {
        return job.getBuildHealth().getScore();
    }

    @Override
    public BlueRun getLatestRun() {
        if(job.getLastBuild() == null){
            return null;
        }
        return AbstractRunImpl.getBlueRun(job.getLastBuild(), this);
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        return job.getEstimatedDuration();
    }

    @Override
    public String getLastSuccessfulRun() {
        if(job.getLastSuccessfulBuild() != null){
            String id = job.getLastSuccessfulBuild().getId();

            return Stapler.getCurrentRequest().getRootPath()+getLink().getHref()+"runs/"+id+"/";
        }
        return null;
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(this, job);
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return getActionProxies(job.getAllActions(), this);
    }

    @Override
    @Navigable
    public BlueQueueContainer getQueue() {
        return new QueueContainerImpl(this);
    }


    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        job.delete();
    }


    @Override
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }

        FavoriteUtil.favoriteJob(job.getFullName(), favoriteAction.isFavorite());
        return FavoriteUtil.getFavorite(job, new Reachable() {
            @Override
            public Link getLink() {
                return AbstractPipelineImpl.this.getLink().ancestor();
            }
        });

    }

    @Override
    public String getFullName(){
        return job.getFullName();
    }

    @Override
    public Link getLink() {
        return OrganizationImpl.INSTANCE.getLink().rel("pipelines").rel(getRecursivePathFromFullName(this));
    }

    public static String getRecursivePathFromFullName(BluePipeline pipeline){
        StringBuilder pipelinePath = new StringBuilder();
        String[] names = pipeline.getFullName().split("/");
        int count = 1;
        if(names.length > 1) { //nested
            for (String n : names) {
                if(count == 1){
                    pipelinePath.append(n);
                }else{
                    pipelinePath.append("/pipelines/").append(n);
                }
                count++;
            }
        }else{
            pipelinePath.append(pipeline.getFullName());
        }
        return pipelinePath.toString();
    }

    public static Collection<BlueActionProxy> getActionProxies(List<? extends Action> actions, Reachable parent){
        List<BlueActionProxy> actionProxies = new ArrayList<>();
        for(Action action:actions){
            if(action == null){
                continue;
            }
            actionProxies.add(new ActionProxiesImpl(action, parent));
        }
        return actionProxies;

    }

    @Navigable
    public Container<Resource> getActivities() {
        return Containers.fromResource(getLink(), Lists.newArrayList(Iterators.concat(getQueue().iterator(), getRuns().iterator())));
    }

    /**
     * Gives underlying Jenkins job
     *
     * @return jenkins job
     */
    public Job getJob(){
        return job;
    }

    @Extension(ordinal = 0)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if (item instanceof Job) {
                return new AbstractPipelineImpl((Job) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            if(context == target && target instanceof Job) {
                return getPipeline(target,parent);
            }
            return null;
        }
    }

    @Override
    public int getNumberOfRunningPipelines(){
        RunList runningJobs =  job.getBuilds().filter(isRunning);
        return Iterators.size(runningJobs.iterator());
    }

    @Override
    public int getNumberOfQueuedPipelines() {
        if(job instanceof BuildableItem) {
            return Iterables.size(Jenkins.getInstance().getQueue().getItems((BuildableItem)job));
        }
        return 0;
    }

    @Override
    public Map<String, Boolean> getPermissions(){
        return getPermissions(job);
    }

    public static Map<String, Boolean> getPermissions(AbstractItem item){
        return ImmutableMap.of(
            BluePipeline.CREATE_PERMISSION, item.getACL().hasPermission(Item.CREATE),
            BluePipeline.READ_PERMISSION, item.getACL().hasPermission(Item.READ),
            BluePipeline.START_PERMISSION, item.getACL().hasPermission(Item.BUILD),
            BluePipeline.STOP_PERMISSION, item.getACL().hasPermission(Item.CANCEL)
        );
    }

    public static final Predicate<Run> isRunning = new Predicate<Run>() {
        public boolean apply(Run r) {
            return r.isBuilding();
        }
    };


}
