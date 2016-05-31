package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;
import org.kohsuke.stapler.json.JsonBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchPipelineImpl extends BlueMultiBranchPipeline {
    /*package*/ final MultiBranchProject mbp;

    public MultiBranchPipelineImpl(MultiBranchProject mbp) {
        this.mbp = mbp;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }


    @Override
    public void favorite(@JsonBody FavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }

        Job job = mbp.getBranch("master");
        if(job == null) {
            throw new ServiceException.UnexpectedErrorException("no master branch to favorite");
        }

        FavoriteUtil.favoriteJob(job, favoriteAction.isFavorite());
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
    @SuppressWarnings("unchecked")
    public int getWeatherScore(){
        /**
         * TODO: this code need cleanup once MultiBranchProject exposes default branch. At present
         *
         * At present we look for master as primary branch, if not found we find the latest build and return
         * its score.
         *
         * If there are no builds taken place 0 score is returned.
         */

        Job j = mbp.getBranch("master");
        if(j == null) {
            j = mbp.getBranch("production");
            if(j == null){ //get latest
                Collection<Job>  jbs = mbp.getAllJobs();
                if(jbs.size() > 0){
                    Job[] jobs = jbs.toArray(new Job[jbs.size()]);
                    Arrays.sort(jobs, new Comparator<Job>() {
                        @Override
                        public int compare(Job o1, Job o2) {
                            long t1 = o1.getLastBuild().getTimeInMillis() + o1.getLastBuild().getDuration();
                            long t2 = o2.getLastBuild().getTimeInMillis() + o2.getLastBuild().getDuration();
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
    public BluePipelineContainer getBranches() {
        return new BranchContainerImpl(this);
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
                if (run.getResult() == result) {
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
            public BluePipeline getPipeline(String name) {
                return null;
            }

            @Override
            public BlueRun get(String name) {
                return null;
            }

            @Override
            public Iterator<BlueRun> iterator() {
                List<BlueRun> c = new ArrayList<>();
                for(final BluePipeline b: getBranches()) {
                    for(final BlueRun r: b.getRuns()) {
                        c.add(r);
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
        };
    }

}
