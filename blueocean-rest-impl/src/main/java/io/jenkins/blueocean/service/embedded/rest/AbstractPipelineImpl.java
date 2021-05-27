package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.User;
import hudson.plugins.favorite.Favorites;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.util.Disabler;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_JOB;

/**
 * Pipeline abstraction implementation. Use it to extend other kind of jenkins jobs
 *
 * @author Vivek Pandey
 */
@Capability(JENKINS_JOB)
public class AbstractPipelineImpl extends BluePipeline {
    private final Job job;
    protected final BlueOrganization organization;

    protected AbstractPipelineImpl(BlueOrganization organization, Job job) {
        this.job = job;
        this.organization = organization;
    }

    @Override
    public String getOrganizationName() {
        return organization.getName();
    }

    @Override
    @NonNull
    public BlueOrganization getOrganization() {
        return organization;
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
        Run run = job.getLastBuild();
        if (run != null) {
            return new AbstractRunImpl<Run>(run, AbstractPipelineImpl.this, organization) {
                @Override
                public Collection<BlueActionProxy> getActions() {
                    return Collections.emptyList();
                }

                @Override
                public BlueTestSummary getTestSummary() {
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        return job.getEstimatedDuration();
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(this, job);
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(job.getAllActions(), this);
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
            throw new ServiceException.BadRequestException("Must provide pipeline name");
        }
        FavoriteUtil.toggle(favoriteAction, job);
        return FavoriteUtil.getFavorite(job, new Reachable() {
            @Override
            public Link getLink() {
                return AbstractPipelineImpl.this.getLink().ancestor();
            }
        });

    }

    @Override
    public String getFullName(){
        return getFullName(organization, job);
    }

    @Override
    public String getFullDisplayName() {
        return getFullDisplayName(organization, job);
    }

    @Override
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "isDisabled will return null if the job type doesn't support it")
    public Boolean getDisabled() {
        return Disabler.isDisabled(job);
    }

    @Override
    public void enable() throws IOException {
        if (getPermissions(job).getOrDefault(BluePipeline.CONFIGURE_PERMISSION, Boolean.FALSE)) {
            Disabler.makeDisabled(job, false);
        }
    }

    @Override
    public void disable() throws IOException {
        if (getPermissions(job).getOrDefault(BluePipeline.CONFIGURE_PERMISSION, Boolean.FALSE)) {
            Disabler.makeDisabled(job, true);
        }
    }

    /**
     * Returns full display name relative to the <code>BlueOrganization</code> base. Each display name is separated by
     * '/' and each display name is url encoded
     *
     * @param org the organization the item belongs to
     * @param item to return the full display name of
     *
     * @return full display name
     */
    public static String getFullDisplayName(@Nullable BlueOrganization org, @NonNull Item item) {
        ItemGroup<?> group = getBaseGroup(org);
        String[] displayNames = Functions.getRelativeDisplayNameFrom(item, group).split(" » ");

        StringBuilder encodedDisplayName=new StringBuilder();
        for(int i=0;i<displayNames.length;i++) {
            if(i!=0) {
                encodedDisplayName.append(String.format("/%s", Util.rawEncode(displayNames[i])));
            }else {
                encodedDisplayName.append(String.format("%s", Util.rawEncode(displayNames[i])));
            }
        }

        return encodedDisplayName.toString();
    }

    /**
     * Returns full name relative to the <code>BlueOrganization</code> base. Each name is separated by '/'
     *
     * @param org the organization the item belongs to
     * @param item to return the full name of
     * @return
     */
    public static String getFullName(@Nullable BlueOrganization org, @NonNull Item item) {
        ItemGroup<?> group = getBaseGroup(org);
        return Functions.getRelativeNameFrom(item, group);
    }

    /**
     * Tries to obtain the base group for a <code>BlueOrganization</code>
     *
     * @param org to get the base group of
     * @return the base group
     */
    public static ItemGroup<?> getBaseGroup(BlueOrganization org) {
        ItemGroup<?> group = null;
        if (org != null && org instanceof AbstractOrganization) {
            group = ((AbstractOrganization) org).getGroup();
        }
        return group;
    }

    @Override
    public Link getLink() {
        return organization.getLink().rel("pipelines").rel(getRecursivePathFromFullName(this));
    }

    /**
     * Calculates the recursive path for the <code>BluePipeline</code>. The path is relative to the org base
     *
     * @param pipeline to get the recursive path from
     * @return the recursive path
     */
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

    @Override
    public List<Object> getParameters() {
        return getParameterDefinitions(job);
    }

    public static List<Object> getParameterDefinitions(Job job){
        ParametersDefinitionProperty pp = (ParametersDefinitionProperty) job.getProperty(ParametersDefinitionProperty.class);
        List<Object> pds = new ArrayList<>();
        if(pp != null){
            for(ParameterDefinition pd : pp.getParameterDefinitions()){
                pds.add(pd);
            }
        }
        return pds;
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
        public BluePipeline getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof Job) {
                return new AbstractPipelineImpl(organization, (Job) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            if(context == target && target instanceof Job) {
                return getPipeline(target, parent, organization);
            }
            return null;
        }
    }

    @Override
    public Map<String, Boolean> getPermissions(){
        return getPermissions(job);
    }

    @Override
    public BluePipelineScm getScm() {
        return null;
    }

    @Override
    public BlueTrendContainer getTrends() {
        return new BlueTrendContainerImpl(this);
    }

    public static Map<String, Boolean> getPermissions(AbstractItem item){
        return ImmutableMap.of(
            BluePipeline.CREATE_PERMISSION, item.getACL().hasPermission(Item.CREATE),
            BluePipeline.CONFIGURE_PERMISSION, item.getACL().hasPermission(Item.CONFIGURE),
            BluePipeline.READ_PERMISSION, item.getACL().hasPermission(Item.READ),
            BluePipeline.START_PERMISSION, item.getACL().hasPermission(Item.BUILD),
            BluePipeline.STOP_PERMISSION, item.getACL().hasPermission(Item.CANCEL)
        );
    }

    public static final Predicate<Run> isRunning = new Predicate<Run>() {
        public boolean apply(Run r) {
            return r != null && r.isBuilding();
        }
    };

    public boolean isFavorite() {
        User user = User.current();
        return user != null && Favorites.isFavorite(user, job);
    }

}
