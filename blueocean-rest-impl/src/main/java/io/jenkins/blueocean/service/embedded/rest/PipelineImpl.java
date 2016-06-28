package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.MultiBranchProject;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.DELETE;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.jenkins.blueocean.service.embedded.rest.PipelineContainerImpl.isMultiBranchProjectJob;

/**
 * @author Kohsuke Kawaguchi
 */

public class PipelineImpl extends BluePipeline {
    /*package*/ final Job job;

    private final ItemGroup folder;

    private final Link parent;

    protected PipelineImpl(ItemGroup folder, Job job, Link parent) {
        this.job = job;
        this.folder = folder;
        this.parent = null;
    }

    public PipelineImpl(ItemGroup folder, Link parent) {
        this(folder, null,parent);
    }

    public PipelineImpl(Job job, Link parent) {
        this(null, job, parent);
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
        return AbstractRunImpl.getBlueRun(job.getLastBuild(), this.getLink());
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
        return new QueueContainerImpl(this, job);
    }

    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        job.delete();
    }


    @Override
    public void favorite(@JsonBody FavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }

        FavoriteUtil.favoriteJob(job.getFullName(), favoriteAction.isFavorite());
    }

    @Override
    public String getFullName(){
        return job.getFullName();
    }

    public BluePipeline getPipelines(String name){
        assert folder != null;
        return getPipeline(folder, name);
    }

    private  BluePipeline getPipeline(ItemGroup itemGroup, String name){
        Item item = itemGroup.getItem(name);
        if(item instanceof BuildableItem){
            if(item instanceof MultiBranchProject){
                return new MultiBranchPipelineImpl((MultiBranchProject) item, getLink());
            }else if(!isMultiBranchProjectJob((BuildableItem) item) && item instanceof Job){
                return new PipelineImpl(itemGroup, (Job) item, parent);
            }
        }else if(item instanceof ItemGroup){
            return new PipelineImpl((ItemGroup) item, null);
        }
        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
    }

    @Override
    public Link getLink() {
        return OrganizationImpl.INSTANCE.getLink().rel("pipelines").rel(getRecursivePathFromFullName(this));
    }

    protected static String getRecursivePathFromFullName(BluePipeline pipeline){
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

    @Extension(ordinal = 1)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if (item instanceof Job) {
                return new PipelineImpl((Job) item, parent.getLink());
            }
            return null;
        }
    }

    public static Collection<BlueActionProxy> getActionProxies(List<? extends Action> actions, Reachable parent){
        List<BlueActionProxy> actionProxies = new ArrayList<>();
        for(Action action:actions){
            if(action == null || !action.getClass().isAnnotationPresent(ExportedBean.class)){
                continue;
            }
            actionProxies.add(new ActionProxiesImpl(action, parent));
        }
        return actionProxies;

    }
    
    // TODO: @vivek/@ivan please fix me the right way !!!
    public static @Nonnull Link getLink(@Nonnull ParameterizedJobMixIn.ParameterizedJob job) {
        Link orgLink = new Link("/rest/organizations/" + OrganizationImpl.INSTANCE.getName());

        if (job instanceof WorkflowJob) {
            ItemGroup<? extends Item> parent = job.getParent();
            if (parent instanceof WorkflowMultiBranchProject) {
                String multiBranchProjectName = ((WorkflowMultiBranchProject) parent).getName();
                return orgLink.rel("pipelines").rel(multiBranchProjectName).rel("branches").rel(job.getName());
            }
        }
        
        return orgLink.rel("pipelines").rel(job.getName());
    }
}
