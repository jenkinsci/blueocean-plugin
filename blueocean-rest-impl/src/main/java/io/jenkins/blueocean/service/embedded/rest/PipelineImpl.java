package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.MultiBranchProject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;

import static io.jenkins.blueocean.rest.Utils.ensureTrailingSlash;
import static io.jenkins.blueocean.service.embedded.rest.PipelineContainerImpl.isMultiBranchProjectJob;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl extends BluePipeline {
    /*package*/ final Job job;

    private final ItemGroup folder;
    protected PipelineImpl(ItemGroup folder, Job job) {
        this.job = job;
        this.folder = folder;
    }

    public PipelineImpl(ItemGroup folder) {
        this(folder, null);
    }

    public PipelineImpl(Job job) {
        this(null, job);
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
        return AbstractRunImpl.getBlueRun(job.getLastBuild());
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        return job.getEstimatedDuration();
    }

    @Override
    public String getLastSuccessfulRun() {
        if(job.getLastSuccessfulBuild() != null){
            String id = job.getLastSuccessfulBuild().getId();
            return String.format("%s%s%s/%s",Stapler.getCurrentRequest().getRootPath(),
                getPathInfo(), "runs",
                job.getLastSuccessfulBuild().getId());
        }
        return null;
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(this, job);
    }

    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        job.delete();
    }

    private String getPathInfo(){
        String path = Stapler.getCurrentRequest().getPathInfo();
        if(!path.endsWith(getName()) && !path.endsWith(getName()+"/")){
            return String.format("%s%s/", ensureTrailingSlash(path), getName());
        }
        return ensureTrailingSlash(path);
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
        return getPipelines(folder, name);
    }

    protected static BluePipeline getPipelines(ItemGroup itemGroup, String name){
        Item item = itemGroup.getItem(name);
        if(item instanceof BuildableItem){
            if(item instanceof MultiBranchProject){
                return new MultiBranchPipelineImpl((MultiBranchProject) item);
            }else if(!isMultiBranchProjectJob((BuildableItem) item) && item instanceof Job){
                return new PipelineImpl(itemGroup, (Job) item);
            }
        }else if(item instanceof ItemGroup){
            return new PipelineImpl((ItemGroup) item, null);
        }
        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
    }
}
