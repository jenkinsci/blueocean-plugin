package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BlueFavoriteResolver;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmSourceImpl;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueIcon;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.BlueScmSource;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.FavoriteImpl;
import io.jenkins.blueocean.service.embedded.util.Disabler;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.BranchProjectFactory;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.impl.pipeline.PipelineJobFilters.isPullRequest;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_SCM;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_MULTI_BRANCH_PROJECT;

/**
 * @author Vivek Pandey
 */
@Capability({JENKINS_MULTI_BRANCH_PROJECT, BLUE_SCM})
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    private final Link self;
    private final BlueOrganization organization;
    private String scriptPath = "Jenkinsfile";

    public MultiBranchPipelineImpl(BlueOrganization organization, MultiBranchProject mbp) {
        this.mbp = mbp;
        this.organization = organization;
        this.self = this.organization.getLink().rel("pipelines").rel(PipelineImpl.getRecursivePathFromFullName(this));
    }


    @Exported(
        name = "scriptPath"
    )
    public String getScriptPath() {
        return scriptPath;
    }

    private void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Nonnull
    @Override
    public BlueOrganization getOrganization() {
        return organization;
    }

    @Override
    public String getOrganizationName() {
        return organization.getName();
    }

    @Override
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        if (favoriteAction == null) {
            throw new ServiceException.BadRequestException("Must provide pipeline name");
        }
        Job job = PrimaryBranch.resolve(mbp);
        if (job == null) {
            throw new ServiceException.BadRequestException("no default branch to favorite");
        }
        FavoriteUtil.toggle(favoriteAction, job);
        return new FavoriteImpl(new BranchImpl(organization, job, getLink().rel("branches")), getLink().rel("favorite"));
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return AbstractPipelineImpl.getPermissions(mbp);
    }

    @Navigable
    @Override
    public BluePipelineScm getScm() {
        return new ScmResourceImpl(mbp, this);
    }

    @Override
    public String getName() {
        return mbp.getName();
    }

    @Override
    public String getDisplayName() {
        return mbp.getDisplayName();
    }

    @Override
    public String getFullName() {
        return AbstractPipelineImpl.getFullName(organization, mbp);
    }

    @Override
    public String getFullDisplayName() {
        return AbstractPipelineImpl.getFullDisplayName(organization, mbp);
    }

    @Override
    public int getTotalNumberOfBranches() {
        return countJobs(false);
    }

    @Override
    public int getNumberOfFailingBranches() {
        return countRunStatus(Result.FAILURE, false);
    }

    @Override
    public int getNumberOfSuccessfulBranches() {
        return countRunStatus(Result.SUCCESS, false);
    }

    @Override
    public int getTotalNumberOfPullRequests() {
        return countJobs(true);
    }

    @Override
    public int getNumberOfFailingPullRequests() {
        return countRunStatus(Result.FAILURE, true);
    }

    @Override
    public int getNumberOfSuccessfulPullRequests() {
        return countRunStatus(Result.SUCCESS, true);
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return new BranchContainerImpl(this, getLink().rel("pipelines"));
    }

    @Override
    public Integer getNumberOfFolders() {
        return 0;
    }

    @Override
    public Integer getNumberOfPipelines() {
        return getTotalNumberOfBranches();
    }

    @Override
    public Integer getWeatherScore() {
        Job j = PrimaryBranch.resolve(mbp);
        return j == null ? 100 : j.getBuildHealth().getScore();
    }

    @Override
    public BlueRun getLatestRun() {
        //For multibranch is a folder that is no run of itself.
        return null;
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        return mbp.getEstimatedDuration();
    }

    @Override
    @Navigable
    public BluePipelineContainer getBranches() {
        return new BranchContainerImpl(this, getLink().rel("branches"));
    }

    @Override
    public Collection<String> getBranchNames() {
        return Collections2.transform(ImmutableList.copyOf(this.getBranches().iterator()), new Function<BluePipeline, String>() {
            @Override
            public String apply(BluePipeline input) {
                return input.getName();
            }
        });
    }

    private int countRunStatus(Result result, boolean pullRequests) {
        Collection<Job> jobs = mbp.getAllJobs();
        int count = 0;
        for (Job j : jobs) {
            if (pullRequests && isPullRequest(j) || !pullRequests && !isPullRequest(j)) {
                j.getBuildStatusUrl();
                Run run = j.getLastBuild();
                if (run != null && run.getResult() == result) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countJobs(boolean pullRequests) {
        Collection<Job> jobs = mbp.getAllJobs();
        int counter = 0;

        for (Job job : jobs) {
            if (pullRequests && isPullRequest(job) || !pullRequests && !isPullRequest(job)) {
                counter += 1;
            }
        }

        return counter;
    }


    @Override
    public BlueRunContainer getRuns() {
        return new MultibranchPipelineRunContainer(this);
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(mbp.getAllActions(), this);
    }

    @Override
    public BlueQueueContainer getQueue() {
        return new MultiBranchPipelineQueueContainer(this);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Extension(ordinal = 2)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public MultiBranchPipelineImpl getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof MultiBranchProject) {
                MultiBranchPipelineImpl mbpi = new MultiBranchPipelineImpl(organization, (MultiBranchProject) item);
                if (item instanceof WorkflowMultiBranchProject) {
                    WorkflowMultiBranchProject wfmbp = (WorkflowMultiBranchProject)item;
                    BranchProjectFactory<WorkflowJob, WorkflowRun> bpf = wfmbp.getProjectFactory();
                    if (bpf instanceof WorkflowBranchProjectFactory) {
                        String sp = ((WorkflowBranchProjectFactory) bpf).getScriptPath();
                        mbpi.setScriptPath(sp);
                    }
                }
                return mbpi;
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            if (context instanceof MultiBranchProject) {
                if (context == target)
                    return getPipeline(context, parent, organization);
                if (context == target.getParent()) {
                    // target is a branch
                    return getPipeline(context, parent, organization).getBranches().get(target.getName());
                }
            }
            return null;
        }

    }

    @Extension(ordinal = 1)
    public static class FavoriteResolverImpl extends BlueFavoriteResolver {
        @Override
        public BlueFavorite resolve(Item item, Reachable parent) {
            if (item instanceof MultiBranchProject) {
                MultiBranchProject project = (MultiBranchProject) item;
                Job job = PrimaryBranch.resolve(project);
                if (job != null) {
                    Resource resource = BluePipelineFactory.resolve(job);
                    Link l = LinkResolver.resolveLink(project);
                    if (l != null) {
                        return new FavoriteImpl(resource, l.rel("favorite"));
                    }
                }
            }
            return null;
        }
    }

    @Override
    public List<Object> getParameters() {
        return null;
    }

    @Override
    public BlueIcon getIcon() {
        return null;
    }

    @Override
    public BlueScmSource getScmSource() {
        return new ScmSourceImpl(mbp);
    }

    @Override
    public BlueTrendContainer getTrends() {
        return null;
    }

    @Override
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "isDisabled will return null if the job type doesn't support it")
    public Boolean getDisabled() {
        return null; // NOT SUPPORTED
    }

    @Override
    public void enable() throws IOException {
        throw new ServiceException.MethodNotAllowedException("Cannot enable this item");
    }

    @Override
    public void disable() throws IOException {
        throw new ServiceException.MethodNotAllowedException("Cannot disable this item");
    }
}
