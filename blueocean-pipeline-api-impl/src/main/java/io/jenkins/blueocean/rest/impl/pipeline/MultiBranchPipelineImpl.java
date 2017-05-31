package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.Utils;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BlueFavoriteResolver;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
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
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.FavoriteImpl;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.MultiBranchProject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.json.JsonBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.impl.pipeline.PipelineJobFilters.isPullRequest;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_MULTI_BRANCH_PROJECT;

/**
 * @author Vivek Pandey
 */
@Capability({JENKINS_MULTI_BRANCH_PROJECT})
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    private static final int MAX_MBP_RUNS_ROWS = Integer.getInteger("MAX_MBP_RUNS_ROWS", 250);

    private final Link self;
    private final BlueOrganization org;

    public MultiBranchPipelineImpl(MultiBranchProject mbp) {
        this.mbp = mbp;
        this.org = OrganizationFactory.getInstance().getContainingOrg((ItemGroup) mbp);
        this.self = org.getLink().rel("pipelines").rel(PipelineImpl.getRecursivePathFromFullName(this));
    }

    @Override
    public String getOrganization() {
        return org.getName();
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
        return new FavoriteImpl(new BranchImpl(job, getLink().rel("branches")), getLink().rel("favorite"));
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
        return AbstractPipelineImpl.getFullName(org, mbp);
    }

    @Override
    public String getFullDisplayName() {
        return AbstractPipelineImpl.getFullDisplayName(org, mbp);
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
        return new BlueRunContainer() {
            @Override
            public Link getLink() {
                return MultiBranchPipelineImpl.this.getLink().rel("runs");
            }


            @Override
            public BlueRun get(String name) {
                return null;
            }

            @Override
            public Iterator<BlueRun> iterator() {
                throw new ServiceException.NotImplementedException("Not implemented");
            }

            /**
             * Fetches maximum up to  MAX_MBP_RUNS_ROWS rows from each branch and does pagination on that.
             *
             * JVM property MAX_MBP_RUNS_ROWS can be used to tune this value to optimize performance for given setup
             */
            @Override
            public Iterator<BlueRun> iterator(int start, int limit) {
                List<BlueRun> c = new ArrayList<>();

                List<BluePipeline> branches;

                // Check for branch filter
                StaplerRequest req = Stapler.getCurrentRequest();
                String branchFilter = null;
                if (req != null) {
                    branchFilter = req.getParameter("branch");
                }

                if (!StringUtils.isEmpty(branchFilter)) {
                    BluePipeline pipeline = getBranches().get(branchFilter);
                    if (pipeline != null) {
                        branches = Collections.singletonList(pipeline);
                    } else {
                        branches = Collections.emptyList();
                    }
                } else {
                    branches = Lists.newArrayList(getBranches().list());
                    sortBranchesByLatestRun(branches);
                }

                for (final BluePipeline b : branches) {
                    Iterator<BlueRun> it = b.getRuns().iterator(0, MAX_MBP_RUNS_ROWS);
                    int count = 0;
                    Utils.skip(it, start);
                    while (it.hasNext() && count++ < limit) {
                        c.add(it.next());
                    }
                }

                Collections.sort(c, new Comparator<BlueRun>() {
                    @Override
                    public int compare(BlueRun o1, BlueRun o2) {
                        return o2.getStartTime().compareTo(o1.getStartTime());
                    }
                });

                return Iterators.limit(c.iterator(), limit);
            }

            private boolean retry(boolean[] retries) {
                //if at least one of the branch needs retry we will retry it
                for (boolean r : retries) {
                    if (r) {
                        return true;
                    }
                }
                return false;
            }

            private int computeLimit(boolean[] retries, int limit) {
                //if at least one of the branch needs retry we will retry it
                int count = 0;
                for (boolean r : retries) {
                    if (r) {
                        count++;
                    }
                }
                if (count == 0) {
                    return 0;
                }
                return limit / count > 0 ? limit / count : 1;
            }

            private int collectRuns(List<BluePipeline> branches, List<BlueRun> runs,
                                    boolean[] retries, int remainingCount, int[] startIndexes, int[] limits) {
                int count = 0;
                for (int i = 0; i < branches.size(); i++) {
                    BluePipeline b = branches.get(i);
                    if (!retries[i]) {
                        continue;
                    }
                    Iterator<BlueRun> it = b.getRuns().iterator(startIndexes[i], limits[i]);
                    int lcount = 0;
                    while (it.hasNext() && count < remainingCount) {
                        lcount++;
                        count++;
                        runs.add(it.next());
                    }
                    if (lcount < limits[i]) { //if its less than l
                        retries[i] = false; //iterator already exhausted so lets not retry next time
                    } else {
                        startIndexes[i] = startIndexes[i] + lcount; //set the new start index for next time
                    }
                }
                return count;
            }


            @Override
            public BlueRun create(StaplerRequest request) {
                throw new ServiceException.NotImplementedException("This action is not supported");
            }
        };
    }

    static void sortBranchesByLatestRun(List<BluePipeline> branches) {
        Collections.sort(branches, new Comparator<BluePipeline>() {
            @Override
            public int compare(BluePipeline o1, BluePipeline o2) {
                Long t1 = o1.getLatestRun() != null ? o1.getLatestRun().getStartTime().getTime() : 0;
                Long t2 = o2.getLatestRun() != null ? o2.getLatestRun().getStartTime().getTime() : 0;

                return t2.compareTo(t1);
            }
        });
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
        public MultiBranchPipelineImpl getPipeline(Item item, Reachable parent) {
            if (item instanceof MultiBranchProject) {
                return new MultiBranchPipelineImpl((MultiBranchProject) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            if (context instanceof MultiBranchProject) {
                if (context == target)
                    return getPipeline(context, parent);
                if (context == target.getParent()) {
                    // target is a branch
                    return getPipeline(context, parent).getBranches().get(target.getName());
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
}
