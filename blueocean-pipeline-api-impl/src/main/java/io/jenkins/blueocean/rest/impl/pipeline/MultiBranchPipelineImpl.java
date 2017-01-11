package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.BlueFavoriteResolver;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.FavoriteImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.json.JsonBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_MULTI_BRANCH_PROJECT;

/**
 * @author Vivek Pandey
 */
@Capability({JENKINS_MULTI_BRANCH_PROJECT})
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    private static final int MAX_MBP_RUNS_ROWS = Integer.getInteger("MAX_MBP_RUNS_ROWS", 250);

    private final Link self;
    public MultiBranchPipelineImpl(MultiBranchProject mbp) {
        this.mbp = mbp;
        this.self = OrganizationImpl.INSTANCE.getLink().rel("pipelines").rel(PipelineImpl.getRecursivePathFromFullName(this));
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }


    @Override
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }
        Job job = FavoriteUtil.resolveDefaultBranch(mbp);
        FavoriteUtil.toggle(favoriteAction, job);
        return new FavoriteImpl(new BranchImpl(job,getLink().rel("branches")), getLink().rel("favorite"));
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return AbstractPipelineImpl.getPermissions(mbp);
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
        return mbp.getFullName();
    }

    @Override
    public String getFullDisplayName() {
        return AbstractPipelineImpl.getFullDisplayName(mbp, null);
    }

    @Override
    public int getTotalNumberOfBranches(){
        return countJobs(false);
    }

    @Override
    public int getNumberOfFailingBranches(){
        return countRunStatus(Result.FAILURE, false);
    }

    @Override
    public int getNumberOfSuccessfulBranches(){
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
    @SuppressWarnings("unchecked")
    public Integer getWeatherScore(){
        /**
         * TODO: this code need cleanup once MultiBranchProject exposes default branch. At present
         *
         * At present we look for master as primary branch, if not found we find the latest build across all branches and
         * return its score.
         *
         * If there are no builds taken place 0 score is returned.
         */

        Job j = mbp.getItem("master");
        if(j == null) {
            j = mbp.getItem("production");
            /**
             * If there are no master or production branch then we return weather score of
             *
             * - Sort latest build of all branches in ascending order
             * - Return the latest
             *
             */
            if(j == null){
                Collection<Job>  jbs = mbp.getAllJobs();
                if(jbs.size() > 0){
                    Job[] jobs = jbs.toArray(new Job[jbs.size()]);
                    Arrays.sort(jobs, new Comparator<Job>() {
                        @Override
                        public int compare(Job o1, Job o2) {
                            long t1 = 0;
                            if(o1.getLastBuild() != null){
                                t1 = o1.getLastBuild().getTimeInMillis() + o1.getLastBuild().getDuration();
                            }

                            long t2 = 0;
                            if(o2.getLastBuild() != null){
                                t2 = o2.getLastBuild().getTimeInMillis() + o2.getLastBuild().getDuration();
                            }

                            if(t1<2){
                                return -1;
                            }else if(t1 > t2){
                                return 1;
                            }else{
                                return 0;
                            }
                        }
                    });

                    return jobs[jobs.length - 1].getBuildHealth().getScore();
                }
            }
        }
        return j == null ? 0 : j.getBuildHealth().getScore();
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
    public String getLastSuccessfulRun() {
        return null;
    }

    @Override
    @Navigable
    public BluePipelineContainer getBranches() {
        return new BranchContainerImpl(this, getLink().rel("branches"));
    }

    @Override
    public Collection<String> getBranchNames() {
        Collection<Job> jobs =  mbp.getAllJobs();
        List<String> branches = new ArrayList<>();
        for(Job j : jobs){
            branches.add(j.getName());
        }
        return branches;
    }

    private int countRunStatus(Result result, boolean pullRequests){
        Collection<Job> jobs = mbp.getAllJobs();
        int count=0;
        for(Job j:jobs){
            if(pullRequests && isPullRequest(j) || !pullRequests && !isPullRequest(j)) {
                j.getBuildStatusUrl();
                Run run = j.getLastBuild();
                if (run!=null && run.getResult() == result) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countJobs(boolean pullRequests) {
        Collection<Job> jobs = mbp.getAllJobs();
        int counter = 0;

        for(Job job: jobs){
            if(pullRequests && isPullRequest(job) || !pullRequests && !isPullRequest(job)) {
                counter += 1;
            }
        }

        return counter;
    }
    private boolean isPullRequest(Job job) {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        return head != null && head.getAction(ChangeRequestAction.class) != null;
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

                List<BluePipeline> branches = Lists.newArrayList(getBranches().list());


                sortBranchesByLatestRun(branches);

                for(final BluePipeline b: getBranches()) {
                    Iterator<BlueRun> it = b.getRuns().iterator(0,MAX_MBP_RUNS_ROWS);
                    int count = 0;
                    Iterators.skip(it, start);
                    while(it.hasNext() && count++ < limit){
                        c.add(it.next());
                    }
                }

                Collections.sort(c, new Comparator<BlueRun>() {
                    @Override
                    public int compare(BlueRun o1, BlueRun o2) {
                        return o2.getStartTime().compareTo(o1.getStartTime());
                    }
                });

                return c.iterator();
            }

            private boolean retry(boolean[] retries){
                //if at least one of the branch needs retry we will retry it
                for(boolean r: retries){
                    if(r){
                        return true;
                    }
                }
                return false;
            }

            private int computeLimit(boolean[] retries, int limit){
                //if at least one of the branch needs retry we will retry it
                int count=0;
                for(boolean r: retries){
                    if(r){
                        count++;
                    }
                }
                if(count == 0){
                    return 0;
                }
                return limit/count > 0 ? limit/count : 1;
            }

            private int collectRuns(List<BluePipeline> branches, List<BlueRun> runs,
                                    boolean[] retries, int remainingCount, int[] startIndexes, int[] limits){
                int count = 0;
                for (int i=0; i < branches.size(); i++) {
                    BluePipeline b = branches.get(i);
                    if(!retries[i]){
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
                    }else{
                        startIndexes[i] = startIndexes[i]+lcount; //set the new start index for next time
                    }
                }
                return count;
            }


            @Override
            public BlueQueueItem create(StaplerRequest request) {
                throw new ServiceException.NotImplementedException("This action is not supported");
            }
        };
    }

    static void sortBranchesByLatestRun(List<BluePipeline> branches){
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
                if (context==target)
                    return getPipeline(context,parent);
                if (context==target.getParent()) {
                    // target is a branch
                    return getPipeline(context,parent).getBranches().get(target.getName());
                }
            }
            return null;
        }

    }

    @Extension(ordinal = 1)
    public static class FavoriteResolverImpl extends BlueFavoriteResolver {
        @Override
        public BlueFavorite resolve(Item item, Reachable parent) {
            if(item instanceof MultiBranchProject){
                MultiBranchProject project = (MultiBranchProject) item;
                Job job = FavoriteUtil.resolveDefaultBranch(project);
                if(job != null){
                    Resource resource = BluePipelineFactory.resolve(job);
                    Link l = LinkResolver.resolveLink(project);
                    if(l != null) {
                        return new FavoriteImpl(resource, l.rel("favorite"));
                    }
                }
            }
            return null;
        }
    }

    @Navigable
    public Container<Resource> getActivities() {
        return new Container<Resource>() {
            @Override
            public Resource get(String name) {
                return null;
            }

            @Override
            public Link getLink() {
                return MultiBranchPipelineImpl.this.getLink().rel("activities");
            }

            @Override
            public Iterator<Resource> iterator() {
                throw new ServiceException.NotImplementedException("Not implemented");
            }

            @Override
            public Iterator<Resource> iterator(int start, int limit) {
                return AbstractPipelineImpl.activityIterator(getQueue(), getRuns(), start, limit);
            }
        };

    }

    @Override
    public List<Object> getParameters() {
        return null;
    }
}
